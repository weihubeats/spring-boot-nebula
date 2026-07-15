package com.nebula.web.boot.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebula.base.utils.JsonUtil;
import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.config.NebulaWebProperties;
import com.nebula.web.boot.enums.ResultCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class NebulaResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final NebulaWebProperties nebulaWebProperties;

    private final ObjectMapper defaultObjectMapper;

    private static final Map<Class<? extends ObjectMapper>, ObjectMapper> MAPPER_CACHE = new ConcurrentHashMap<>();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean hasAnnotation = returnType.hasMethodAnnotation(NebulaResponseBody.class) ||
            returnType.getContainingClass().isAnnotationPresent(NebulaResponseBody.class);
        boolean isNotWrapped = returnType.getParameterType() != NebulaResponse.class;
        return hasAnnotation && isNotWrapped;
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
        ServerHttpResponse response) {

        NebulaResponse<Object> baseResponse = new NebulaResponse<>();
        baseResponse.setCode(nebulaWebProperties.getResponseCode());
        baseResponse.setMsg(ResultCode.SUCCESS.getMessage());
        baseResponse.setData(body);

        NebulaResponseBody annotation = returnType.getMethodAnnotation(NebulaResponseBody.class);
        if (annotation == null) {
            annotation = returnType.getContainingClass().getAnnotation(NebulaResponseBody.class);
        }

        if (body instanceof String) {
            ObjectMapper mapper = getTargetObjectMapper(annotation);
            try {
                // 设置响应头，防止中文乱码，保持与原设计的兼容
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return mapper.writeValueAsString(baseResponse);
            } catch (JsonProcessingException e) {
                log.error("Nebula SDK: 序列化 String 返回值失败", e);
                throw new RuntimeException("JSON 序列化异常", e);
            }
        }

        if (annotation != null && annotation.objectMapper() != JsonUtil.JacksonObjectMapper.class) {
            try {
                ObjectMapper mapper = getTargetObjectMapper(annotation);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return mapper.writeValueAsString(baseResponse);
            } catch (JsonProcessingException e) {
                log.error("Nebula SDK: 自定义 ObjectMapper 序列化失败", e);
                throw new RuntimeException("JSON 序列化异常", e);
            }
        }

        return baseResponse;

    }

    private ObjectMapper getTargetObjectMapper(NebulaResponseBody annotation) {
        if (annotation == null || annotation.objectMapper() == JsonUtil.JacksonObjectMapper.class) {
            return defaultObjectMapper;
        }
        return MAPPER_CACHE.computeIfAbsent(annotation.objectMapper(), clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("Nebula SDK: 无法实例化自定义 ObjectMapper [{}]", clazz.getName(), e);
                return defaultObjectMapper; // 降级使用默认
            }
        });
    }
}
