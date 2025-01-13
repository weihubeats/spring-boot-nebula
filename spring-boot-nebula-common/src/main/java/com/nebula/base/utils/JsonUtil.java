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
import com.fasterxml.jackson.databind.*;
import com.nebula.base.exception.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

/**
 * @author : wh
 * @date : 2023/5/18 10:27
 * @description:
 */
@Slf4j
public class JsonUtil {
    
    /**
     * 将对象序列化成json字符串
     *
     * @param value javaBean
     * @param <T>   T 泛型标记
     * @return jsonString json字符串
     */
    public static <T> String toJSONString(T value) {
        try {
            return getInstance().writeValueAsString(value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    public static <T> String toJSONString(ObjectMapper objectMapper, T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 对象转 JsonNode
     * @param entity
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> JsonNode obj2JsonNode(T entity) throws Exception {
        return getInstance().valueToTree(entity);
    }
    
    /**
     * 将对象序列化成 json byte 数组
     *
     * @param object javaBean
     * @return jsonString json字符串
     */
    public static byte[] toJsonAsBytes(Object object) {
        try {
            return getInstance().writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json反序列化成对象
     *
     * @param content   content
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    public static <T> T json2JavaBean(String content, Class<T> valueType) {
        try {
            return getInstance().readValue(content, valueType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 将json反序列化成对象
     *
     * @param content       content
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    public static <T> T json2JavaBean(String content, TypeReference<T> typeReference) {
        try {
            return getInstance().readValue(content, typeReference);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json byte 数组反序列化成对象
     *
     * @param bytes     json bytes
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    public static <T> T json2JavaBean(byte[] bytes, Class<T> valueType) {
        try {
            return getInstance().readValue(bytes, valueType);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json反序列化成对象
     *
     * @param bytes         bytes
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    public static <T> T json2JavaBean(byte[] bytes, TypeReference<T> typeReference) {
        try {
            return getInstance().readValue(bytes, typeReference);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json反序列化成对象
     *
     * @param in        InputStream
     * @param valueType class
     * @param <T>       T 泛型标记
     * @return Bean
     */
    public static <T> T json2JavaBean(InputStream in, Class<T> valueType) {
        try {
            return getInstance().readValue(in, valueType);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json反序列化成对象
     *
     * @param in            InputStream
     * @param typeReference 泛型类型
     * @param <T>           T 泛型标记
     * @return Bean
     */
    public static <T> T json2JavaBean(InputStream in, TypeReference<T> typeReference) {
        try {
            return getInstance().readValue(in, typeReference);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    public static <T> List<T> json2Array(String json, Class<T> valueTypeRef) {
        List<T> objectList = Collections.emptyList();
        if (DataUtils.isEmpty(json)) {
            return objectList;
        }
        JavaType javaType = getInstance().getTypeFactory().constructParametricType(List.class, valueTypeRef);
        try {
            return getInstance().readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return objectList;
    }
    
    public static <T> List<T> jsonNode2Array(JsonNode jsonNode, Class<T> clazz) {
        ObjectReader reader = getInstance().readerForListOf(clazz);
        List<T> objectList = Collections.emptyList();
        try {
            return reader.readValue(jsonNode);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return objectList;
    }
    
    /**
     * json to Map
     * @param content
     * @return
     */
    public static Map<String, Object> json2Map(String content) {
        try {
            return getInstance().readValue(content, Map.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    public static <T> Map<String, T> json2Map(String content, Class<T> valueTypeRef) {
        try {
            Map<String, Map<String, Object>> map = getInstance().readValue(content, new TypeReference<Map<String, Map<String, Object>>>() {
            });
            Map<String, T> result = new HashMap<>(16);
            for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
                result.put(entry.getKey(), toPojo(entry.getValue(), valueTypeRef));
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    public static <T> T toPojo(Map fromValue, Class<T> toValueType) {
        return getInstance().convertValue(fromValue, toValueType);
    }
    
    /**
     * 将json字符串转成 JsonNode
     *
     * @param jsonString jsonString
     * @return jsonString json字符串
     */
    public static JsonNode json2JsonNode(String jsonString) {
        try {
            return getInstance().readTree(jsonString);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * jsonNode to String
     * @param jsonNode
     * @return
     */
    public static String jsonNodeToString(JsonNode jsonNode) {
        try {
            return getInstance().writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json字节数组转成 JsonNode
     *
     * @param content content
     * @return jsonString json字符串
     */
    public static JsonNode byte2JsonNode(byte[] content) {
        try {
            return getInstance().readTree(content);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json字符串转成 JsonNode
     *
     * @param in InputStream
     * @return jsonString json字符串
     */
    public static JsonNode json2JsonNode(InputStream in) {
        try {
            return getInstance().readTree(in);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json字符串转成 JsonNode
     *
     * @param content content
     * @return jsonString json字符串
     */
    public static JsonNode json2JsonNode(byte[] content) {
        try {
            return getInstance().readTree(content);
        } catch (IOException e) {
            throw ExceptionUtil.unchecked(e);
        }
    }
    
    /**
     * 将json字符串转成 JsonNode
     *
     * @param jsonParser JsonParser
     * @return jsonString json字符串
     */
    public static JsonNode json2JsonNode(JsonParser jsonParser) {
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
            // 设置地点为中国
            super.setLocale(CHINA);
            super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            // 去掉默认的时间戳格式
            super.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            // 设置为中国上海时区
            super.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            // 序列化时，日期的统一格式
            super.setDateFormat(new SimpleDateFormat(TimeUtil.YYYYMMddHHmmss, Locale.CHINA));
            // 序列化处理
            super.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
            super.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
            super.findAndRegisterModules();
            // 失败处理
            super.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            super.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // 单引号处理
            super.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            // 反序列化时，属性不存在的兼容处理
            super.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            // 日期格式化
            super.registerModule(new JacksonTimeModule());
            super.findAndRegisterModules();
        }
        
        @Override
        public ObjectMapper copy() {
            return super.copy();
        }
        
    }
    
}
