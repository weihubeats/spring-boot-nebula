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
 
package com.nebula.base.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nebula.base.exception.ExceptionUtil;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;

/**
 * Jackson JSON 工具。
 * <p>
 * Class 反序列化失败返回 {@code null}；TypeReference / 字节序列化失败抛出运行时异常。
 */
@Slf4j
public final class JsonUtil {
    
    private JsonUtil() {
    }
    
    /**
     * 对象序列化为 JSON 字符串；失败返回 {@code null}。
     */
    public static <T> String toJson(T value) {
        try {
            return getInstance().writeValueAsString(value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 使用指定 ObjectMapper 序列化为 JSON 字符串；失败返回 {@code null}。
     */
    public static <T> String toJson(ObjectMapper objectMapper, T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 对象转为 JsonNode。
     */
    public static <T> JsonNode toJsonNode(T value) {
        return getInstance().valueToTree(value);
    }
    
    /**
     * 对象序列化为 JSON 字节数组。
     */
    public static byte[] toJsonBytes(Object value) {
        try {
            return getInstance().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * JSON 字符串反序列化为对象；失败返回 {@code null}。
     */
    public static <T> T fromJson(String content, Class<T> valueType) {
        if (DataUtils.isEmpty(content) || DataUtils.isEmpty(valueType)) {
            return null;
        }
        try {
            return getInstance().readValue(content, valueType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * JSON 字符串反序列化为泛型对象。
     */
    public static <T> T fromJson(String content, TypeReference<T> typeReference) {
        try {
            return getInstance().readValue(content, typeReference);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * JSON 字节数组反序列化为对象；失败返回 {@code null}。
     */
    public static <T> T fromJson(byte[] bytes, Class<T> valueType) {
        if (DataUtils.isEmpty(bytes) || DataUtils.isEmpty(valueType)) {
            return null;
        }
        try {
            return getInstance().readValue(bytes, valueType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * JSON 字节数组反序列化为泛型对象。
     */
    public static <T> T fromJson(byte[] bytes, TypeReference<T> typeReference) {
        try {
            return getInstance().readValue(bytes, typeReference);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 从输入流反序列化为对象；失败返回 {@code null}。
     */
    public static <T> T fromJson(InputStream in, Class<T> valueType) {
        if (DataUtils.isEmpty(in) || DataUtils.isEmpty(valueType)) {
            return null;
        }
        try {
            return getInstance().readValue(in, valueType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 从输入流反序列化为泛型对象。
     */
    public static <T> T fromJson(InputStream in, TypeReference<T> typeReference) {
        try {
            return getInstance().readValue(in, typeReference);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * JSON 字符串反序列化为 List；失败返回空列表。
     */
    public static <T> List<T> fromJsonList(String json, Class<T> elementType) {
        if (DataUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        JavaType javaType = getInstance().getTypeFactory().constructParametricType(List.class, elementType);
        try {
            return getInstance().readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }
    
    /**
     * JsonNode 转为 List；失败返回空列表。
     */
    public static <T> List<T> toList(JsonNode jsonNode, Class<T> elementType) {
        if (DataUtils.isEmpty(jsonNode)) {
            return Collections.emptyList();
        }
        ObjectReader reader = getInstance().readerForListOf(elementType);
        try {
            return reader.readValue(jsonNode);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }
    
    /**
     * JSON 字符串反序列化为 Map；失败返回 {@code null}。
     */
    public static Map<String, Object> fromJsonMap(String content) {
        if (DataUtils.isEmpty(content)) {
            return null;
        }
        try {
            return getInstance().readValue(content, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * JSON 字符串反序列化为值为指定类型的 Map；失败返回 {@code null}。
     */
    public static <T> Map<String, T> fromJsonMap(String content, Class<T> valueType) {
        if (DataUtils.isEmpty(content)) {
            return null;
        }
        try {
            Map<String, Map<String, Object>> map = getInstance().readValue(content,
                    new TypeReference<Map<String, Map<String, Object>>>() {
                    });
            Map<String, T> result = new HashMap<>(16);
            for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
                result.put(entry.getKey(), convert(entry.getValue(), valueType));
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * Map / 中间结构转换为目标类型（基于 ObjectMapper#convertValue）。
     */
    public static <T> T convert(Map<?, ?> fromValue, Class<T> toValueType) {
        return getInstance().convertValue(fromValue, toValueType);
    }
    
    /**
     * JSON 字符串解析为 JsonNode。
     */
    public static JsonNode fromJsonNode(String json) {
        try {
            return getInstance().readTree(json);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 从输入流解析为 JsonNode。
     */
    public static JsonNode fromJsonNode(InputStream in) {
        try {
            return getInstance().readTree(in);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 从字节数组解析为 JsonNode。
     */
    public static JsonNode fromJsonNode(byte[] content) {
        try {
            return getInstance().readTree(content);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 从 JsonParser 解析为 JsonNode。
     */
    public static JsonNode fromJsonNode(JsonParser jsonParser) {
        try {
            return getInstance().readTree(jsonParser);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    public static ObjectMapper getInstance() {
        return JacksonHolder.INSTANCE;
    }
    
    private static class JacksonHolder {
        
        private static final ObjectMapper INSTANCE = new JacksonObjectMapper();
    }
    
    public static class JacksonObjectMapper extends ObjectMapper {
        
        private static final long serialVersionUID = 4288193147502386170L;
        
        private static final Locale CHINA = Locale.CHINA;
        
        public JacksonObjectMapper() {
            super();
            super.setLocale(CHINA);
            super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            super.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            super.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            super.setDateFormat(new SimpleDateFormat(TimeUtil.YYYY_MM_DD_HH_MM_SS, Locale.CHINA));
            super.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
            super.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
            super.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            super.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            super.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            super.findAndRegisterModules();
            super.registerModule(new JacksonTimeModule());
        }
    }
}
