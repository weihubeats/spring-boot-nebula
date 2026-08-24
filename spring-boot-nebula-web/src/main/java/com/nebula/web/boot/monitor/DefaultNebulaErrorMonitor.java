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
 
package com.nebula.web.boot.monitor;

import com.nebula.base.utils.JsonUtil;
import com.nebula.web.boot.config.NebulaWebProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认错误监控编排器。
 * <p>职责：归一化告警 key → 频率限制 → 组装 {@link AlertMessage} → 交给 {@link NebulaAlertChannel} 推送。
 * 渠道选择、模板渲染、长度限制均由渠道实现负责。
 */
@Slf4j
public class DefaultNebulaErrorMonitor implements NebulaErrorMonitor {
    
    private final NebulaWebProperties nebulaWebProperties;
    
    private final NebulaAlertLimiter alertLimiter;
    
    private final NebulaAlertChannel alertChannel;
    
    private static final Pattern DYNAMIC_SEGMENT_PATTERN = Pattern.compile("/\\d+(?=/|$)");
    
    private static final int MAX_BODY_BYTES = 15 * 1024;
    
    public DefaultNebulaErrorMonitor(NebulaWebProperties nebulaWebProperties,
                                     NebulaAlertLimiter alertLimiter,
                                     NebulaAlertChannel alertChannel) {
        this.nebulaWebProperties = nebulaWebProperties;
        this.alertLimiter = alertLimiter;
        this.alertChannel = alertChannel;
    }
    
    @Override
    public void monitorError(HttpServletRequest request, Exception ex) {
        try {
            String key = buildAlertKey(request, ex);
            if (nebulaWebProperties.getMonitor().getLimit().isEnabled() && !alertLimiter.tryAcquire(key)) {
                log.warn("告警限流: key={}, 已达到限制, 跳过本次告警", key);
                return;
            }
            AlertMessage message = new AlertMessage(request.getRequestURI(),
                    JsonUtil.toJson(request.getParameterMap()),
                    readRequestBody(request), ex);
            alertChannel.send(message);
        } catch (Exception e) {
            // 告警失败绝不影响业务响应
            log.error("告警发送失败, ex={}", ex.getClass().getSimpleName(), e);
        }
    }
    
    private String buildAlertKey(HttpServletRequest request, Exception ex) {
        String normalizedUri = DYNAMIC_SEGMENT_PATTERN.matcher(request.getRequestURI()).replaceAll("/{id}");
        return ex.getClass().getName() + ":" + normalizedUri;
    }
    
    private static String readRequestBody(HttpServletRequest request) {
        try {
            byte[] buf = request.getInputStream().readNBytes(MAX_BODY_BYTES);
            if (buf.length == 0) {
                return "";
            }
            if (buf.length < MAX_BODY_BYTES) {
                return new String(buf, StandardCharsets.UTF_8);
            }
            int end = Utf8TextUtils.findUtf8Boundary(buf, 0, buf.length);
            return new String(buf, 0, end, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}
