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

import com.nebula.i18n.config.NebulaI18nProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

/**
 * 请求语言解析：自定义 header（X-Lang）→ query 参数（lang）→ Accept-Language → 默认语言。
 * <p>优先级可通过 {@code nebula.i18n.prefer-accept-language} 调整；
 * 解析结果写入 {@link org.springframework.context.i18n.LocaleContextHolder}（由 DispatcherServlet 完成）。
 * 不支持的语言回退默认语言。
 */
public class NebulaLocaleResolver extends AbstractLocaleResolver {
    
    private final NebulaI18nProperties properties;
    
    private final Locale defaultLocale;
    
    private final Set<Locale> supported;
    
    public NebulaLocaleResolver(NebulaI18nProperties properties) {
        this.properties = properties;
        this.defaultLocale = properties.resolveDefaultLocale();
        this.supported = new HashSet<>(properties.resolveSupportedLocales());
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale = null;
        if (properties.isPreferAcceptLanguage()) {
            locale = resolveAcceptLanguage(request);
        }
        if (locale == null) {
            locale = resolveHeader(request);
        }
        if (locale == null) {
            locale = resolveParam(request);
        }
        if (locale == null && !properties.isPreferAcceptLanguage()) {
            locale = resolveAcceptLanguage(request);
        }
        if (locale == null) {
            locale = defaultLocale;
        }
        return supported.contains(locale) ? locale : defaultLocale;
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("NebulaLocaleResolver is read-only, do not change locale per request");
    }
    
    private Locale resolveHeader(HttpServletRequest request) {
        if (!StringUtils.hasText(properties.getHeaderName())) {
            return null;
        }
        String value = request.getHeader(properties.getHeaderName());
        return parse(value);
    }
    
    private Locale resolveParam(HttpServletRequest request) {
        if (!StringUtils.hasText(properties.getParamName())) {
            return null;
        }
        String value = request.getParameter(properties.getParamName());
        return parse(value);
    }
    
    private Locale resolveAcceptLanguage(HttpServletRequest request) {
        String value = request.getHeader("Accept-Language");
        return parse(value);
    }
    
    private Locale parse(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String tag = value.trim().split(",")[0].trim();
        if (tag.isEmpty()) {
            return null;
        }
        return Locale.forLanguageTag(tag.replace('_', '-'));
    }
    
}
