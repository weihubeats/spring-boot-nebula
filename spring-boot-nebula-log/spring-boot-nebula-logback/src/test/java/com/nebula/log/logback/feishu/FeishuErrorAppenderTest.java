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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.nebula.base.utils.JsonUtil;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FeishuErrorAppenderTest {
    
    private FeishuErrorAppender appender;
    
    @AfterEach
    void tearDown() {
        if (appender != null && appender.isStarted()) {
            appender.stop();
        }
    }
    
    private FeishuErrorAppender newAppender() {
        FeishuErrorAppender a = new FeishuErrorAppender();
        a.setContext(new LoggerContext());
        return a;
    }
    
    @Test
    void sendsErrorAsync() throws Exception {
        List<String> sent = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        appender = newAppender();
        appender.setWebhookUrl("https://example.com/hook");
        appender.setTitle("ut");
        appender.setSender((url, text) -> {
            sent.add(text);
            latch.countDown();
        });
        appender.start();
        
        LoggingEvent error = event(Level.ERROR, "boom");
        appender.doAppend(error);
        
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(1, sent.size());
        String card = sent.get(0);
        assertTrue(card.contains("logger: test.logger"));
        assertTrue(card.contains("message: boom"));
        assertTrue(card.contains("ut ERROR"));
        assertTrue(card.contains("red"));
        assertNotNull(JsonUtil.json2JsonNode(card));
    }
    
    @Test
    void ignoresInfoLevel() throws Exception {
        List<String> sent = new CopyOnWriteArrayList<>();
        appender = newAppender();
        appender.setWebhookUrl("https://example.com/hook");
        appender.setSender((url, text) -> sent.add(text));
        appender.start();
        
        appender.doAppend(event(Level.INFO, "ok"));
        Thread.sleep(300);
        assertTrue(sent.isEmpty());
    }
    
    @Test
    void rateLimitDropsExtra() throws Exception {
        List<String> sent = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);
        
        appender = newAppender();
        appender.setWebhookUrl("https://example.com/hook");
        appender.setMaxPerMinute(2);
        appender.setSender((url, text) -> {
            sent.add(text);
            latch.countDown();
        });
        appender.start();
        
        appender.doAppend(event(Level.ERROR, "e1"));
        appender.doAppend(event(Level.ERROR, "e2"));
        appender.doAppend(event(Level.ERROR, "e3"));
        
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        Thread.sleep(200);
        assertEquals(2, sent.size());
    }
    
    @Test
    void sendFailureDoesNotPropagate() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        appender = newAppender();
        appender.setWebhookUrl("https://example.com/hook");
        appender.setSender((url, text) -> {
            latch.countDown();
            throw new IllegalStateException("network down");
        });
        appender.start();
        
        appender.doAppend(event(Level.ERROR, "fail-me"));
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(appender.isStarted());
    }
    
    @Test
    void buildCardEscapesSpecialCharsViaJsonUtil() {
        FeishuWebhookClient client = new FeishuWebhookClient();
        String card = client.buildCard("logger", "a\"b\nc", "stack", "title");
        assertNotNull(JsonUtil.json2JsonNode(card));
        assertTrue(card.contains("a\\\"b\\nc"));
    }
    
    @Test
    void tryAcquireRespectsLimit() {
        appender = newAppender();
        appender.setWebhookUrl("https://example.com/hook");
        appender.setMaxPerMinute(1);
        appender.setSender((url, text) -> {
        });
        appender.start();
        
        assertTrue(appender.tryAcquire());
        assertFalse(appender.tryAcquire());
    }
    
    private static LoggingEvent event(Level level, String msg) {
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test.logger");
        return new LoggingEvent("fqcn", logger, level, msg, null, null);
    }
}
