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
 
package com.nebula.web.boot.error;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.DisposableBean;

/**
 * 飞书告警滑动窗口限流器。
 * <p>key 级别原子操作，非 key 间不互相阻塞；空闲 key 由后台线程定时回收，避免长期运行内存泄漏。
 */
public class FeishuAlertLimiter implements NebulaAlertLimiter, DisposableBean {
    
    private final int windowSeconds;
    private final int maxCount;
    
    private final ConcurrentHashMap<String, AlertWindow> windows = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService cleanupScheduler;
    
    private static final class AlertWindow {
        
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private volatile long lastAccess;
    }
    
    public FeishuAlertLimiter(int windowSeconds, int maxCount) {
        if (windowSeconds <= 0) {
            throw new IllegalArgumentException("nebula.web.monitor-limit-window-seconds must be positive");
        }
        if (maxCount <= 0) {
            throw new IllegalArgumentException("nebula.web.monitor-limit-max-count must be positive");
        }
        this.windowSeconds = windowSeconds;
        this.maxCount = maxCount;
        long idleMs = Math.max(windowSeconds * 2L, 60L) * 1000L;
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "feishu-alert-limiter-cleaner");
            thread.setDaemon(true);
            return thread;
        });
        this.cleanupScheduler.scheduleWithFixedDelay(() -> cleanup(idleMs), idleMs, idleMs / 2, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 尝试获取一个窗口内的告警配额。
     *
     * @param key 告警维度，如 异常类:归一化URI
     * @return true=配额内可发送；false=已达窗口上限应丢弃
     */
    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        AtomicBoolean granted = new AtomicBoolean(false);
        windows.compute(key, (k, window) -> {
            if (window == null) {
                window = new AlertWindow();
            }
            window.lastAccess = now;
            Deque<Long> deque = window.timestamps;
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
                deque.pollFirst();
            }
            if (deque.size() >= maxCount) {
                return window;
            }
            deque.offerLast(now);
            granted.set(true);
            return window;
        });
        return granted.get();
    }
    
    private void cleanup(long idleMs) {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().lastAccess > idleMs);
    }
    
    public void close() {
        cleanupScheduler.shutdownNow();
    }
    
    @Override
    public void destroy() {
        close();
    }
}