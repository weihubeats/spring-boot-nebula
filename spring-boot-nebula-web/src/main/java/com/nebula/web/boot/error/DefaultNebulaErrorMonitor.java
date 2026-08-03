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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebula.alert.feishu.FeiShuRoot;
import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.JsonUtil;
import com.nebula.web.boot.config.NebulaWebProperties;
import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

/**
 * @author : wh
 * @date : 2025/3/25
 * @description:
 */
@Slf4j
public class DefaultNebulaErrorMonitor implements NebulaErrorMonitor {
    
    // todo 先写死
    private final FeiShuRoot feiShuRoot;
    
    private final NebulaWebProperties nebulaWebProperties;
    
    private static final int FEISHU_MESSAGE_HASH_MAX_LENGTH = 15 * 1024;
    
    /**
     * 路径中的纯数字段视为动态段（如 /user/{id}），归一化保证同质异常不因参数值分裂 key
     */
    private static final Pattern DYNAMIC_SEGMENT_PATTERN = Pattern.compile("/\\d+(?=/|$)");
    
    private final NebulaAlertLimiter alertLimiter;
    
    public DefaultNebulaErrorMonitor(FeiShuRoot feiShuRoot, NebulaWebProperties nebulaWebProperties,
                                     NebulaAlertLimiter alertLimiter) {
        this.feiShuRoot = feiShuRoot;
        this.nebulaWebProperties = nebulaWebProperties;
        this.alertLimiter = alertLimiter;
    }
    
    @Override
    public void monitorError(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            String key = buildAlertKey(request, ex);
            if (nebulaWebProperties.isMonitorLimitEnabled() && !alertLimiter.tryAcquire(key)) {
                log.warn("飞书告警限流: key={}, 已达到限制, 跳过本次告警", key);
                return;
            }
            sendFeiShuErrorMsg(request, response, handler, ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    private String buildAlertKey(HttpServletRequest request, Exception ex) {
        String normalizedUri = DYNAMIC_SEGMENT_PATTERN.matcher(request.getRequestURI()).replaceAll("/{id}");
        return ex.getClass().getSimpleName() + ":" + normalizedUri;
    }
    
    /**
     * 发送飞书错误信息
     */
    public void sendFeiShuErrorMsg(HttpServletRequest request, HttpServletResponse response, Object handler,
                                   Exception ex) throws IOException {
        String uri = request.getRequestURI();
        String body = new String(StreamUtils.copyToByteArray(request.getInputStream()), request.getCharacterEncoding());
        
        // 删掉多余的转义字符
        String errorStackMsg = stackTraceToJsonValue(ex);
        if (errorStackMsg.getBytes(StandardCharsets.UTF_8).length > FEISHU_MESSAGE_HASH_MAX_LENGTH) {
            errorStackMsg = errorStackMsg.substring(0, new String(new byte[FEISHU_MESSAGE_HASH_MAX_LENGTH]).length());
        }
        if (body.getBytes(StandardCharsets.UTF_8).length > FEISHU_MESSAGE_HASH_MAX_LENGTH) {
            body = body.substring(0, new String(new byte[FEISHU_MESSAGE_HASH_MAX_LENGTH]).length());
        }
        if (DataUtils.isNotEmpty(body)) {
            body = body.substring(1, body.length() - 2);
        }
        String jsonString = JsonUtil.toJSONString(request.getParameterMap());
        if (DataUtils.isNotEmpty(jsonString)) {
            jsonString = jsonString.replace("\"", "\\\"");
        }
        feiShuRoot.sendRichTextAsync(nebulaWebProperties.getMonitorUrl(), readUtf8String("config/feishu.json"), jsonString, body, errorStackMsg, uri);
    }
    
    public static String stackTraceToJsonValue(Throwable ex) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            
            // 使用 Jackson 处理转义
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(stackTrace).replace("\"", "");
        } catch (Exception e) {
            return "Error formatting stack trace: " + e.getMessage();
        }
    }
    
    private static String readUtf8String(String path) throws IOException {
        try (InputStream inputStream = DefaultNebulaErrorMonitor.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
    
}
