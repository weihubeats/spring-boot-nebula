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
 
package com.nebula.i18n.remote;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.AbstractMessageSource;

/**
 * 基于 {@link RemoteMessageLoader} 缓存的 MessageSource。
 * <p>未命中的 key 返回 null，由组合 MessageSource 继续向本地资源兜底。
 * Locale 精确匹配失败时降级到语言（如 zh_CN → zh）。
 */
@Slf4j
public class RemoteMessageSource extends AbstractMessageSource {
    
    private final RemoteMessageLoader loader;
    
    private final AtomicReference<Map<Locale, Properties>> cache = new AtomicReference<>(new HashMap<>());
    
    public RemoteMessageSource(RemoteMessageLoader loader) {
        this.loader = loader;
    }
    
    /**
     * 重新加载远程文案；失败时保留旧缓存。
     */
    public synchronized void refresh() {
        try {
            Map<Locale, Properties> loaded = loader.load();
            if (loaded != null && !loaded.isEmpty()) {
                cache.set(loaded);
                log.info("[nebula-i18n] remote messages refreshed, locales={}", loaded.keySet());
            }
        } catch (Exception e) {
            log.warn("[nebula-i18n] remote messages refresh failed, keep old cache", e);
        }
    }
    
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        Map<Locale, Properties> snapshot = cache.get();
        String message = lookup(snapshot, code, locale);
        if (message == null) {
            return null;
        }
        return createMessageFormat(message, locale);
    }
    
    private String lookup(Map<Locale, Properties> snapshot, String code, Locale locale) {
        if (snapshot.isEmpty()) {
            return null;
        }
        Properties exact = snapshot.get(locale);
        if (exact != null && exact.containsKey(code)) {
            return exact.getProperty(code);
        }
        // 降级：zh_CN → zh
        Properties language = snapshot.get(new Locale(locale.getLanguage()));
        if (language != null && language.containsKey(code)) {
            return language.getProperty(code);
        }
        return null;
    }
    
}
