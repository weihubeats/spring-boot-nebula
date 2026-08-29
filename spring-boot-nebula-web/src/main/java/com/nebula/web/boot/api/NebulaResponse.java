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
 
package com.nebula.web.boot.api;

import com.nebula.web.boot.config.NebulaWebProperties;
import com.nebula.web.boot.enums.ResultCode;
import com.nebula.web.boot.exception.BizException;
import com.nebula.web.boot.exception.RpcException;
import com.nebula.web.common.utils.SpringBeanUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description: 统一响应；code 对外可为 Integer 或 String（由协议层配置决定）
 */
@NoArgsConstructor
@Getter
@Setter
public class NebulaResponse<T> implements Serializable {
    
    /**
     * 状态码（协议层：Integer 或 String）
     */
    private Object code;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 返回消息
     */
    private String msg;
    
    private NebulaResponse(IErrorCode resultCode) {
        this(resultCode, null, resolveMessage(resultCode));
    }
    
    private NebulaResponse(IErrorCode resultCode, String msg) {
        this(resultCode, null, msg);
    }
    
    private NebulaResponse(IErrorCode resultCode, T data) {
        this(resultCode, data, resolveMessage(resultCode));
    }
    
    private NebulaResponse(IErrorCode resultCode, T data, String msg) {
        this(toWireCode(resultCode.getCode()), data, msg);
    }
    
    private NebulaResponse(Object code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }
    
    /**
     * 请求是否成功
     */
    public static boolean isSuccess(@Nullable NebulaResponse<?> result) {
        return Optional.ofNullable(result)
                .map(x -> matchesCode(ResultCode.SUCCESS.getCode(), x.code))
                .orElse(Boolean.FALSE);
    }
    
    /**
     * 获取 data
     */
    public T data() {
        if (isBizException(this)) {
            throw new BizException(this.msg);
        }
        if (isNotSuccess(this)) {
            throw new RpcException(this.msg);
        }
        return this.data;
    }
    
    private boolean isBizException(NebulaResponse<T> nebulaResponse) {
        return Optional.ofNullable(nebulaResponse)
                .map(x -> matchesCode(ResultCode.BIZ_EXCEPTION.getCode(), x.code))
                .orElse(Boolean.FALSE);
    }
    
    /**
     * 请求是否失败
     */
    public static boolean isNotSuccess(@Nullable NebulaResponse<?> result) {
        return !NebulaResponse.isSuccess(result);
    }
    
    /**
     * 返回R
     *
     * @param data 数据
     * @param msg  消息
     * @param <T>  T 泛型标记
     * @return R
     */
    public static <T extends Serializable> NebulaResponse<T> data(T data, String msg) {
        return data(HttpServletResponse.SC_OK, data, msg);
    }
    
    /**
     * 返回R
     *
     * @param code 状态码（内部 int，写出前会转协议层 code）
     * @param data 数据
     * @param msg  消息
     * @param <T>  T 泛型标记
     * @return R
     */
    public static <T extends Serializable> NebulaResponse<T> data(int code, T data, String msg) {
        return new NebulaResponse<>(toWireCode(code), data, data == null ? "no data" : msg);
    }
    
    public static <T> NebulaResponse<T> fail(IErrorCode resultCode, String msg) {
        return new NebulaResponse<>(resultCode, msg);
    }
    
    public static <T> NebulaResponse<T> fail(IErrorCode resultCode) {
        return new NebulaResponse<>(resultCode, resolveMessage(resultCode));
    }
    
    /**
     * 返回R
     *
     * @param msg 消息
     * @param <T> T 泛型标记
     * @return R
     */
    public static <T> NebulaResponse<T> fail(String msg) {
        return new NebulaResponse<>(ResultCode.FAILURE, msg);
    }
    
    /**
     * 一次性缓存的 code 映射配置，避免每次请求都做全量 bean 类型扫描。
     * 上下文可能在类加载后才就绪，故未解析前（null）每次调用都会尝试解析一次；
     * 无上下文时 containsBean 仅做空判断，开销可忽略。
     */
    private static volatile NebulaWebProperties cachedWebProperties;
    
    /**
     * 内部 int → 对外协议 code
     */
    static Object toWireCode(int code) {
        NebulaWebProperties properties = cachedWebProperties;
        if (properties == null) {
            properties = resolveWebProperties();
        }
        if (properties != null) {
            return properties.toWireCode(code);
        }
        return code;
    }
    
    private static NebulaWebProperties resolveWebProperties() {
        try {
            if (SpringBeanUtils.containsBean(NebulaWebProperties.class)) {
                NebulaWebProperties properties = SpringBeanUtils.getBean(NebulaWebProperties.class);
                cachedWebProperties = properties;
                return properties;
            }
        } catch (Exception ignored) {
            // 无 Spring 环境时保持 int
        }
        return null;
    }
    
    /**
     * 仅供测试重置缓存
     */
    static void resetCodeResolverForTest() {
        cachedWebProperties = null;
    }
    
    /**
     * 解析错误码消息：存在消息 key（显式指定或自动生成）且 MessageSource 可用时按请求语言翻译，
     * 否则回退默认 message。翻译失败时同样回退默认 message，不抛异常。
     */
    static String resolveMessage(IErrorCode resultCode) {
        String key = resultCode.resolveMessageKey();
        if (key == null) {
            return resultCode.getMessage();
        }
        return translate(key, null, resultCode.getMessage());
    }
    
    /**
     * 按当前请求语言翻译文案；MessageSource 不可用或未命中时返回默认文案。
     *
     * @param code          消息 key
     * @param args          占位符参数
     * @param defaultMessage 未命中时的兜底文案
     * @return 翻译后文案
     */
    public static String translate(String code, Object[] args, String defaultMessage) {
        try {
            if (SpringBeanUtils.containsBean(MessageSource.class)) {
                MessageSource messageSource = SpringBeanUtils.getBean(MessageSource.class);
                return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
            }
        } catch (Exception ignored) {
            // 无 Spring 环境或解析失败时回退默认文案
        }
        return defaultMessage;
    }
    
    /**
     * 判断响应 code 是否与内部错误码匹配（兼容协议层映射后的 String/Integer）
     */
    static boolean matchesCode(int internalCode, Object wireCode) {
        if (Objects.equals(internalCode, wireCode)) {
            return true;
        }
        return Objects.equals(toWireCode(internalCode), wireCode);
    }
    
}
