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
 
package com.nebula.feign.log;

import com.nebula.feign.config.NebulaFeignProperties;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.Util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;

/**
 * Feign 日志过滤器：打印请求方法/URL/参数体、响应状态/体、耗时；超时则打慢调用告警。
 */
@Slf4j
public class NebulaFeignLogFilter implements Client {
    
    private static final int MAX_BODY_LOG_LENGTH = 2048;
    
    private final Client delegate;
    private final NebulaFeignProperties properties;
    
    public NebulaFeignLogFilter(Client delegate, NebulaFeignProperties properties) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.properties = Objects.requireNonNull(properties, "properties");
    }
    
    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        long startNanos = System.nanoTime();
        Charset charset = resolveCharset(request);
        String requestBody = bodyToString(request.body(), charset);
        try {
            Response response = delegate.execute(request, options);
            long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            byte[] responseBytes = readBody(response);
            logRequest(request, costMs, requestBody, response.status(),
                    bodyToString(responseBytes, StandardCharsets.UTF_8));
            logSlowCallIfNecessary(request, costMs, requestBody);
            if (Objects.isNull(response.body())) {
                return response;
            }
            return response.toBuilder().body(responseBytes).build();
        } catch (IOException ex) {
            long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            logAt(LogLevel.WARN, "Feign {} {} cost={}ms requestBody={} error={}",
                    request.httpMethod(), request.url(), costMs, truncate(requestBody), ex.getMessage());
            logSlowCallIfNecessary(request, costMs, requestBody);
            throw ex;
        }
    }
    
    private void logRequest(Request request, long costMs, String requestBody, int status, String responseBody) {
        LogLevel level = properties.getLog().getLevel();
        if (Objects.isNull(level) || level == LogLevel.OFF) {
            return;
        }
        logAt(level, "Feign {} {} cost={}ms requestBody={} responseStatus={} responseBody={}",
                request.httpMethod(), request.url(), costMs,
                truncate(requestBody), status, truncate(responseBody));
    }
    
    private void logSlowCallIfNecessary(Request request, long costMs, String requestBody) {
        NebulaFeignProperties.Slow slow = properties.getLog().getSlow();
        if (Objects.isNull(slow) || !slow.isEnabled()) {
            return;
        }
        long thresholdMs = slow.getThresholdMillis();
        if (thresholdMs <= 0 || costMs < thresholdMs) {
            return;
        }
        LogLevel level = Objects.nonNull(slow.getLevel()) ? slow.getLevel() : LogLevel.ERROR;
        if (level == LogLevel.OFF) {
            return;
        }
        logAt(level,
                "Feign slow call alert {} {} cost={}ms threshold={}ms requestBody={}",
                request.httpMethod(), request.url(), costMs, thresholdMs, truncate(requestBody));
    }
    
    static void logAt(LogLevel level, String format, Object... args) {
        switch (level) {
            case TRACE -> log.trace(format, args);
            case DEBUG -> log.debug(format, args);
            case INFO -> log.info(format, args);
            case WARN -> log.warn(format, args);
            case ERROR, FATAL -> log.error(format, args);
            default -> {
            }
        }
    }
    
    private static byte[] readBody(Response response) throws IOException {
        if (Objects.isNull(response.body())) {
            return new byte[0];
        }
        return Util.toByteArray(response.body().asInputStream());
    }
    
    private static Charset resolveCharset(Request request) {
        Charset charset = request.charset();
        return Objects.nonNull(charset) ? charset : StandardCharsets.UTF_8;
    }
    
    private static String bodyToString(byte[] body, Charset charset) {
        if (Objects.isNull(body) || body.length == 0) {
            return "";
        }
        return new String(body, charset);
    }
    
    private static String truncate(String value) {
        if (Objects.isNull(value) || value.length() <= MAX_BODY_LOG_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_BODY_LOG_LENGTH) + "...(truncated)";
    }
}
