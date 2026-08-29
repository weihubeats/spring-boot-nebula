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
 
package com.nebula.i18n.core;

import com.nebula.web.common.utils.SpringBeanUtils;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.MessageSource;

/**
 * 国际化消息门面，供业务代码在任意位置取当前请求语言的文案。
 * <p>未装配 MessageSource 或查询失败时回退返回 key 本身，不抛异常。
 */
public final class NebulaI18nMessage {
    
    private NebulaI18nMessage() {
    }
    
    /**
     * 取当前请求语言的文案（Locale 取自 {@link LocaleContextHolder}）。
     *
     * @param code 消息 key
     * @param args 占位符参数
     * @return 文案；未命中返回 key 本身
     */
    public static String get(String code, Object... args) {
        return get(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * 取指定语言的文案。
     *
     * @param code   消息 key
     * @param args   占位符参数
     * @param locale 语言
     * @return 文案；未命中返回 key 本身
     */
    public static String get(String code, Object[] args, Locale locale) {
        return resolve(code, args, locale, null);
    }
    
    /**
     * 取指定语言的文案，未命中时返回默认文案。
     */
    public static String getOrDefault(String code, Object[] args, String defaultMessage, Locale locale) {
        return resolve(code, args, locale, defaultMessage);
    }
    
    private static String resolve(String code, Object[] args, Locale locale, String defaultMessage) {
        try {
            if (SpringBeanUtils.containsBean(MessageSource.class)) {
                MessageSource messageSource = SpringBeanUtils.getBean(MessageSource.class);
                return messageSource.getMessage(code, args, defaultMessage, locale);
            }
        } catch (Exception ignored) {
            // 无 Spring 环境或解析失败时回退
        }
        return defaultMessage != null ? defaultMessage : code;
    }
    
}
