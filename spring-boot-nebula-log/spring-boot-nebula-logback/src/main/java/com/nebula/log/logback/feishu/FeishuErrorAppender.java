/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.nebula.log.logback.feishu;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.nebula.log.logback.desensitize.DesensitizeRuntime;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import lombok.AccessLevel;
import lombok.Setter;

/**
 * Logback appender that forwards ERROR (and above) events to a Feishu bot webhook
 * as an interactive rich-text card (same style as web {@code config/feishu.json}).
 *
 * <p>Sends asynchronously with a bounded queue and a simple per-minute rate limit.
 * Failures are reported via Logback status ({@link #addError}) to avoid recursive logging.
 */
public class FeishuErrorAppender extends AppenderBase<ILoggingEvent> {
    
    private static final int DEFAULT_QUEUE_SIZE = 256;
    private static final int DEFAULT_MAX_PER_MINUTE = 10;
    private static final int DEFAULT_TRUNCATE = 2048;
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
    
    @Setter
    private String webhookUrl;
    @Setter
    private String title = "nebula";
    @Setter
    private int maxPerMinute = DEFAULT_MAX_PER_MINUTE;
    @Setter
    private int queueSize = DEFAULT_QUEUE_SIZE;
    
    private FeishuWebhookClient client;
    /**
     * Visible for tests: inject a custom sender instead of HTTP. Args are (webhookUrl, cardJson).
     */
    @Setter(AccessLevel.PACKAGE)
    private BiConsumer<String, String> sender;
    private BlockingQueue<String> queue;
    private Thread worker;
    private volatile boolean running;
    
    private final Object rateLock = new Object();
    private long windowStartMs = System.currentTimeMillis();
    private final AtomicInteger windowCount = new AtomicInteger();
    
    @Override
    public void start() {
        if (Objects.isNull(webhookUrl) || webhookUrl.isBlank()) {
            addError("FeishuErrorAppender webhookUrl is required");
            return;
        }
        if (maxPerMinute <= 0) {
            addError("FeishuErrorAppender maxPerMinute must be > 0");
            return;
        }
        try {
            client = new FeishuWebhookClient();
        } catch (Exception e) {
            addError("FeishuErrorAppender failed to load card template: " + e.getMessage(), e);
            return;
        }
        if (Objects.isNull(sender)) {
            sender = (url, cardJson) -> {
                try {
                    client.sendRichText(url, cardJson);
                } catch (Exception e) {
                    throw new IllegalStateException("Feishu send failed: " + e.getMessage(), e);
                }
            };
        }
        queue = new LinkedBlockingQueue<>(Math.max(1, queueSize));
        running = true;
        worker = new Thread(this::drainQueue, "nebula-feishu-log");
        worker.setDaemon(true);
        worker.start();
        super.start();
    }
    
    @Override
    public void stop() {
        running = false;
        if (Objects.nonNull(worker)) {
            worker.interrupt();
            long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2);
            while (Objects.nonNull(queue) && queue.isEmpty() && System.currentTimeMillis() < deadline) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                worker.join(Math.max(1, deadline - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        super.stop();
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        if (Objects.isNull(event) || !event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            return;
        }
        if (!tryAcquire()) {
            return;
        }
        String cardJson = buildCard(event);
        if (!queue.offer(cardJson)) {
            addError("FeishuErrorAppender queue full, dropping message");
        }
    }
    
    private void drainQueue() {
        while (running) {
            try {
                String cardJson = queue.poll(500, TimeUnit.MILLISECONDS);
                if (Objects.isNull(cardJson)) {
                    continue;
                }
                try {
                    sender.accept(webhookUrl, cardJson);
                } catch (Exception e) {
                    addError("Failed to send Feishu alert: " + e.getMessage(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    boolean tryAcquire() {
        synchronized (rateLock) {
            long now = System.currentTimeMillis();
            if (now - windowStartMs >= WINDOW_MS) {
                windowStartMs = now;
                windowCount.set(0);
            }
            return windowCount.incrementAndGet() <= maxPerMinute;
        }
    }
    
    String buildCard(ILoggingEvent event) {
        String loggerName = truncate(event.getLoggerName());
        String message = truncate(event.getFormattedMessage());
        String stack = "-";
        if (Objects.nonNull(event.getThrowableProxy())) {
            stack = truncate(ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }
        message = DesensitizeRuntime.apply(message);
        stack = DesensitizeRuntime.apply(stack);
        String header = truncate(title + " " + event.getLevel());
        return client.buildCard(loggerName, message, stack, header);
    }
    
    private static String truncate(String value) {
        if (Objects.isNull(value)) {
            return "";
        }
        if (value.length() <= DEFAULT_TRUNCATE) {
            return value;
        }
        return value.substring(0, DEFAULT_TRUNCATE) + "...(truncated)";
    }
}
