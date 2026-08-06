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

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Logback extension settings (desensitize + Feishu ERROR alert).
 *
 * <pre>
 * nebula:
 *   log:
 *     desensitize:
 *       enabled: true
 *       disable-rules:
 *         - bankCard
 *     feishu:
 *       enabled: true
 *       webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
 *       title: my-app
 *       max-per-minute: 10
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "nebula.log")
public class NebulaLogProperties {
    
    private final Desensitize desensitize = new Desensitize();
    
    private final Feishu feishu = new Feishu();
    
    @Data
    public static class Desensitize {
        
        /**
         * Whether to mask sensitive fragments in {@code %msg}.
         */
        private boolean enabled = true;
        
        /**
         * Built-in rule names to skip: mobile, idCard, bankCard, email, secretKey.
         */
        private List<String> disableRules = new ArrayList<>();
    }
    
    @Data
    public static class Feishu {
        
        /**
         * Whether to register {@code FeishuErrorAppender} on the root logger.
         */
        private boolean enabled = false;
        
        /**
         * Feishu bot webhook URL.
         */
        private String webhookUrl;
        
        /**
         * Alert title prefix.
         */
        private String title = "nebula";
        
        /**
         * Max ERROR alerts sent per minute.
         */
        private int maxPerMinute = 10;
        
        /**
         * Async send queue capacity.
         */
        private int queueSize = 256;
    }
}
