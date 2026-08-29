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

import com.nebula.i18n.config.NebulaI18nProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 基于 HTTP 拉取远程 properties 文件的 {@link RemoteMessageLoader}。
 * <p>URL 模板支持 {basename}、{locale} 占位符，如
 * {@code http://config.xxx/i18n/{basename}_{locale}.properties}。
 */
@Slf4j
public class HttpRemoteMessageLoader implements RemoteMessageLoader {
    
    private final OkHttpClient client;
    
    private final String urlTemplate;
    
    private final String basename;
    
    private final List<Locale> locales;
    
    /**
     * @param properties 远程加载相关配置
     */
    public HttpRemoteMessageLoader(NebulaI18nProperties properties) {
        NebulaI18nProperties.Remote remote = properties.getRemote();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(remote.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(remote.getTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        this.urlTemplate = remote.getUrlTemplate();
        this.basename = properties.getBasename();
        this.locales = properties.resolveSupportedLocales();
    }
    
    @Override
    public Map<Locale, Properties> load() {
        Map<Locale, Properties> result = new HashMap<>();
        for (Locale locale : locales) {
            String url = buildUrl(locale);
            Properties props = fetch(url);
            if (props != null) {
                result.put(locale, props);
            }
        }
        return result;
    }
    
    private Properties fetch(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("[nebula-i18n] remote fetch failed, url={}, code={}", url, response.code());
                return null;
            }
            Properties props = new Properties();
            try (InputStream in = response.body().byteStream()) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
            log.info("[nebula-i18n] remote messages loaded, url={}, size={}", url, props.size());
            return props;
        } catch (IOException e) {
            log.warn("[nebula-i18n] remote fetch error, url={}", url, e);
            return null;
        }
    }
    
    private String buildUrl(Locale locale) {
        return urlTemplate
                .replace("{basename}", basename)
                .replace("{locale}", locale.toString());
    }
    
}
