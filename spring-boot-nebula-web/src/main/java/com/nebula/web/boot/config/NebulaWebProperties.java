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
     * 报警 webhook
     */
    private String monitorUrl;
    
    /**
     * 是否开启报警
     */
    private boolean monitorOpen = false;
    
    /**
     * 报警类型
     */
    private String monitorType;
    
    /**
     * 是否开启飞书告警频率限制
     */
    private boolean monitorLimitEnabled = true;
    
    /**
     * 限流窗口（秒）
     */
    private int monitorLimitWindowSeconds = 60;
    
    /**
     * 限流窗口内同 key 最大告警次数
     */
    private int monitorLimitMaxCount = 3;
    
    /**
     * Redis 限流 key 前缀（仅 Redis 限流时使用）
     */
    private String monitorLimitKeyPrefix = "nebula:alert:rate:";
    
    /**
     * Redis 限流 key TTL（秒，需 ≥ 窗口时间）
     */
    private int monitorLimitExpireSeconds = 120;
    
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
