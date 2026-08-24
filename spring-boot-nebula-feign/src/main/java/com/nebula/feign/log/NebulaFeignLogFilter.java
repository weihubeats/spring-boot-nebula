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
import feign.RequestTemplate;
import feign.Response;
import feign.Target;
import feign.Util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;

/**
 * Feign 日志过滤器：打印请求方法/URL/参数体、响应状态/体、耗时；超时则打慢调用告警。
 *
 * <p>请求体与响应体各占一行，便于复制；单次请求只输出一条日志（内部多行）。
 */
@Slf4j
public class NebulaFeignLogFilter implements Client {
    
    private final Client delegate;
    private final NebulaFeignProperties properties;
    private final int maxBodyLength;
    
    public NebulaFeignLogFilter(Client delegate, NebulaFeignProperties properties) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.maxBodyLength = properties.getLog().getMaxBodyLength();
    }
    
    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        long startNanos = System.nanoTime();
        Charset charset = resolveCharset(request);
        // 仅在需要打印请求/慢调用日志时才读取请求体，避免无谓开销
        boolean bodyLoggingNeeded = isRequestLoggingEnabled() || isSlowCallEnabled();
        String requestBody = bodyLoggingNeeded ? bodyToString(request.body(), charset) : "";
        try {
            Response response = delegate.execute(request, options);
            long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            if (!isRequestLoggingEnabled()) {
                // 日志关闭时不读取响应体（大响应整包读入内存无意义），直接透传
                logSlowCallIfNecessary(request, costMs, requestBody);
                return response;
            }
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
            logAt(LogLevel.WARN, "{}", formatError(request, costMs, requestBody, ex.getMessage()));
            logSlowCallIfNecessary(request, costMs, requestBody);
            throw ex;
        }
    }
    
    private boolean isRequestLoggingEnabled() {
        LogLevel level = properties.getLog().getLevel();
        return Objects.nonNull(level) && level != LogLevel.OFF;
    }
    
    private boolean isSlowCallEnabled() {
        NebulaFeignProperties.Slow slow = properties.getLog().getSlow();
        return Objects.nonNull(slow) && slow.isEnabled() && slow.getThresholdMillis() > 0;
    }
    
    private void logRequest(Request request, long costMs, String requestBody, int status, String responseBody) {
        LogLevel level = properties.getLog().getLevel();
        if (Objects.isNull(level) || level == LogLevel.OFF) {
            return;
        }
        logAt(level, "{}", formatRequest(request, costMs, requestBody, status, responseBody));
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
        logAt(level, "{}", formatSlow(request, costMs, requestBody, thresholdMs));
    }
    
    /**
     * 组装请求/响应日志（单条多行：请求体、响应状态、响应体各占一行）。
     */
    String formatRequest(Request request, long costMs, String requestBody, int status, String responseBody) {
        return new StringBuilder(256)
                .append("Feign [").append(clientName(request)).append("] ")
                .append(request.httpMethod()).append(' ').append(request.url())
                .append(" cost=").append(costMs).append("ms")
                .append('\n').append("requestBody=").append(truncate(requestBody))
                .append('\n').append("responseStatus=").append(status)
                .append('\n').append("responseBody=").append(truncate(responseBody))
                .toString();
    }
    
    /**
     * 组装异常日志（单条多行）。
     */
    String formatError(Request request, long costMs, String requestBody, String error) {
        return new StringBuilder(128)
                .append("Feign [").append(clientName(request)).append("] ")
                .append(request.httpMethod()).append(' ').append(request.url())
                .append(" cost=").append(costMs).append("ms error=").append(error)
                .append('\n').append("requestBody=").append(truncate(requestBody))
                .toString();
    }
    
    /**
     * 组装慢调用告警日志（单条多行）。
     */
    String formatSlow(Request request, long costMs, String requestBody, long thresholdMs) {
        return new StringBuilder(128)
                .append("Feign slow call alert [").append(clientName(request)).append("] ")
                .append(request.httpMethod()).append(' ').append(request.url())
                .append(" cost=").append(costMs).append("ms threshold=").append(thresholdMs).append("ms")
                .append('\n').append("requestBody=").append(truncate(requestBody))
                .toString();
    }
    
    /**
     * 解析 Feign Client 名称（如 {@code userClient}），取不到时返回 {@code unknown}。
     */
    static String clientName(Request request) {
        if (Objects.isNull(request)) {
            return "unknown";
        }
        try {
            RequestTemplate template = request.requestTemplate();
            Target<?> target = Objects.isNull(template) ? null : template.feignTarget();
            if (Objects.isNull(target)) {
                return "unknown";
            }
            String name = target.name();
            return Objects.isNull(name) || name.isBlank() ? "unknown" : name;
        } catch (RuntimeException ex) {
            return "unknown";
        }
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
    
    String truncate(String value) {
        if (Objects.isNull(value) || value.length() <= maxBodyLength) {
            return maskSensitive(value);
        }
        return maskSensitive(value.substring(0, maxBodyLength)) + "...(truncated)";
    }
    
    /**
     * 掩码常见敏感字段（password/token/secret 等）的 JSON 值，避免凭证进日志。
     */
    static String maskSensitive(String value) {
        if (Objects.isNull(value) || value.isEmpty()) {
            return value;
        }
        Matcher matcher = SENSITIVE_PATTERN.matcher(value);
        return matcher.replaceAll("$1***$2");
    }
    
    private static final java.util.regex.Pattern SENSITIVE_PATTERN = java.util.regex.Pattern.compile(
            "(\"(?:password|passwd|pwd|token|access_token|secret|accessKey|access_key|apiKey|api_key)\"\\s*:\\s*\")[^\"]*(\")",
            java.util.regex.Pattern.CASE_INSENSITIVE);
}
