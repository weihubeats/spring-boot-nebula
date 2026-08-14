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

package com.nebula.feign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;

/**
 * Feign 模块配置。
 *
 * <pre>
 * nebula:
 *   feign:
 *     log:
 *       enabled: true
 *       level: INFO
 *       max-body-length: 8192
 *       slow:
 *         enabled: true
 *         threshold-millis: 3000
 *         level: ERROR
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "nebula.feign")
public class NebulaFeignProperties {

    private Log log = new Log();

    @Data
    public static class Log {

        /**
         * 是否启用 Feign 请求/响应日志过滤器。
         */
        private boolean enabled = true;

        /**
         * 普通请求日志级别。
         */
        private LogLevel level = LogLevel.INFO;

        /**
         * 请求/响应体日志最大打印长度（字符），超过则截断。默认 8192。
         */
        private int maxBodyLength = 8192;

        public void setMaxBodyLength(int maxBodyLength) {
            if (maxBodyLength < 0) {
                throw new IllegalArgumentException("maxBodyLength must not be negative, but was: " + maxBodyLength);
            }
            this.maxBodyLength = maxBodyLength;
        }

        /**
         * 慢调用告警配置。
         */
        private Slow slow = new Slow();
    }

    @Data
    public static class Slow {

        /**
         * 是否启用慢调用告警。
         */
        private boolean enabled = true;

        /**
         * 慢调用阈值（毫秒），耗时超过该值则打印告警日志。默认 3000ms（3 秒）。
         */
        private long thresholdMillis = 3000L;

        /**
         * 慢调用告警日志级别。
         */
        private LogLevel level = LogLevel.ERROR;
    }
}
