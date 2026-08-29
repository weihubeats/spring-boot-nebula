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
 
package com.nebula.i18n.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 国际化模块配置。
 */
@ConfigurationProperties(prefix = "nebula.i18n")
@Data
public class NebulaI18nProperties {
    
    /**
     * 是否启用国际化模块。
     */
    private boolean enabled = true;
    
    /**
     * 默认语言。
     */
    private String defaultLocale = Locale.SIMPLIFIED_CHINESE.toString();
    
    /**
     * 本地资源文件 basename，如 i18n/messages。
     */
    private String basename = "i18n/messages";
    
    /**
     * 支持的语言列表，用于远程文案加载与本地资源校验。
     */
    private List<String> supportedLocales = new ArrayList<>(List.of(
            Locale.SIMPLIFIED_CHINESE.toString(),
            Locale.US.toString()));
    
    /**
     * 自定义语言 header 名（如 X-Lang），为空则不启用该来源。
     */
    private String headerName = "X-Lang";
    
    /**
     * query 参数名（如 lang），为空则不启用该来源。
     */
    private String paramName = "lang";
    
    /**
     * 是否优先使用 Accept-Language（优先于自定义 header/param）。
     */
    private boolean preferAcceptLanguage = false;
    
    /**
     * 文案找不到时是否回退默认语言。
     */
    private boolean fallbackToDefault = true;
    
    /**
     * 资源文件编码。
     */
    private String charset = "UTF-8";
    
    /**
     * 远程文案加载配置。
     */
    private Remote remote = new Remote();
    
    /**
     * 远程文案加载配置。
     */
    @Data
    public static class Remote {
        
        /**
         * 是否启用远程文案加载。
         */
        private boolean enabled = false;
        
        /**
         * 远程 properties URL 模板，支持 {basename}、{locale} 占位符。
         * 如 http://config.xxx/i18n/{basename}_{locale}.properties
         */
        private String urlTemplate;
        
        /**
         * 定时刷新间隔（秒），0 表示关闭定时刷新。
         */
        private long refreshIntervalSeconds = 0;
        
        /**
         * 超时（秒）。
         */
        private long timeoutSeconds = 5;
    }
    
    /**
     * 支持的 Locale 列表。
     */
    public List<Locale> resolveSupportedLocales() {
        List<Locale> locales = new ArrayList<>();
        for (String tag : supportedLocales) {
            locales.add(Locale.forLanguageTag(tag.replace('_', '-')));
        }
        return locales;
    }
    
    /**
     * 默认 Locale。
     */
    public Locale resolveDefaultLocale() {
        return Locale.forLanguageTag(defaultLocale.replace('_', '-'));
    }
    
}
