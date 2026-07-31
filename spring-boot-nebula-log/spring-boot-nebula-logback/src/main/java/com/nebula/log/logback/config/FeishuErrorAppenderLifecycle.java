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
 
package com.nebula.log.logback.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.nebula.log.logback.feishu.FeishuErrorAppender;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * Registers / deregisters {@link FeishuErrorAppender} from {@link NebulaLogProperties}.
 */
@RequiredArgsConstructor
public class FeishuErrorAppenderLifecycle implements SmartLifecycle {
    
    public static final String APPENDER_NAME = "NEBULA_FEISHU";
    
    private final NebulaLogProperties properties;
    @Getter
    private FeishuErrorAppender appender;
    private volatile boolean running;
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        NebulaLogProperties.Feishu feishu = properties.getFeishu();
        if (Objects.isNull(feishu.getWebhookUrl()) || feishu.getWebhookUrl().isBlank()) {
            return;
        }
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            return;
        }
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        if (Objects.nonNull(root.getAppender(APPENDER_NAME))) {
            running = true;
            return;
        }
        FeishuErrorAppender feishuAppender = new FeishuErrorAppender();
        feishuAppender.setContext(context);
        feishuAppender.setName(APPENDER_NAME);
        feishuAppender.setWebhookUrl(feishu.getWebhookUrl());
        feishuAppender.setTitle(feishu.getTitle());
        feishuAppender.setMaxPerMinute(feishu.getMaxPerMinute());
        feishuAppender.setQueueSize(feishu.getQueueSize());
        feishuAppender.setConnectTimeoutMs(feishu.getConnectTimeoutMs());
        feishuAppender.setReadTimeoutMs(feishu.getReadTimeoutMs());
        feishuAppender.start();
        if (!feishuAppender.isStarted()) {
            return;
        }
        root.addAppender(feishuAppender);
        this.appender = feishuAppender;
        this.running = true;
    }
    
    @Override
    public void stop() {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            running = false;
            return;
        }
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        if (Objects.nonNull(appender)) {
            root.detachAppender(appender);
            appender.stop();
            appender = null;
        } else {
            root.detachAppender(APPENDER_NAME);
        }
        running = false;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }
}
