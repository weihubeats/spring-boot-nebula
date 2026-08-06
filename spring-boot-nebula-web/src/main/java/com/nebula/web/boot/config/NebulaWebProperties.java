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
 
package com.nebula.web.boot.config;

import com.nebula.web.boot.enums.ResultCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : wh
 * @date : 2025/3/12
 * @description:
 */
@ConfigurationProperties(prefix = "nebula.web")
@Data
public class NebulaWebProperties {
    
    /**
     * 成功响应对外 code。支持数字（200）或字符串（Success）。
     * <p>YAML 示例：{@code nebula.web.response-code: 200} 或 {@code Success}
     */
    private String responseCode = String.valueOf(ResultCode.SUCCESS.getCode());
    
    /**
     * 内部 int 错误码 → 对外协议 code 映射（成功/失败均可用）。
     * <p>未命中时：成功码走 {@link #responseCode}，其余直接返回 int。
     * <pre>
     * nebula.web.code-mapping:
     *   200: Success
     *   400: Failure
     *   500: Error
     * </pre>
     */
    private Map<Integer, String> codeMapping = new LinkedHashMap<>();
    
    /**
     * 监控告警配置
     */
    private Monitor monitor = new Monitor();
    
    /**
     * 全局异常处理配置
     */
    private ExceptionHandler exceptionHandler = new ExceptionHandler();
    
    /**
     * 将内部 int 错误码转换为对外写出的协议 code（Integer 或 String）
     */
    public Object toWireCode(int code) {
        if (Objects.nonNull(codeMapping) && codeMapping.containsKey(code)) {
            return parseWireValue(codeMapping.get(code));
        }
        if (code == ResultCode.SUCCESS.getCode()) {
            return parseWireValue(responseCode);
        }
        return code;
    }
    
    /**
     * 全局异常处理（{@code NebulaRestExceptionHandler}）配置。
     */
    @Data
    public static class ExceptionHandler {
        
        /**
         * 是否注册全局 {@code NebulaRestExceptionHandler}。
         * <p>项目已有自己的 {@code @RestControllerAdvice} 时建议关闭，避免异常响应格式被劫持。
         */
        private boolean enabled = true;
    }
    
    /**
     * 监控告警配置。
     */
    @Data
    public static class Monitor {
        
        /**
         * 告警渠道类型，如 feishu
         */
        private String type;
        
        /**
         * 是否开启告警
         */
        private boolean open = false;
        
        /**
         * 告警 webhook
         */
        private String url;
        
        /**
         * 告警频率限制配置
         */
        private Limit limit = new Limit();
    }
    
    /**
     * 告警频率限制配置。
     */
    @Data
    public static class Limit {
        
        /**
         * 是否开启告警频率限制
         */
        private boolean enabled = true;
        
        /**
         * 限流窗口（秒）
         */
        private int windowSeconds = 60;
        
        /**
         * 窗口内同 key 最大告警次数
         */
        private int maxCount = 3;
        
        /**
         * 限流存储：local（单实例内存）或 redis（多实例共享）
         */
        private String storage = "local";
        
        /**
         * Redis 限流 key 前缀（仅 storage=redis 时使用）
         */
        private String keyPrefix = "nebula:alert:rate:";
        
        /**
         * Redis 限流 key TTL（秒，需 ≥ 窗口时间）
         */
        private int expireSeconds = 120;
    }
    
    /**
     * 纯数字字符串解析为 Integer（JSON number），否则保留 String（JSON string）
     */
    public static Object parseWireValue(String raw) {
        if (Objects.isNull(raw) || raw.isBlank()) {
            return ResultCode.SUCCESS.getCode();
        }
        String trimmed = raw.trim();
        if (trimmed.matches("-?\\d+")) {
            return Integer.valueOf(trimmed);
        }
        return trimmed;
    }
    
}
