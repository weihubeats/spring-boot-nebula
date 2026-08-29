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

import static org.assertj.core.api.Assertions.assertThat;

import com.nebula.i18n.config.NebulaI18nProperties;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * {@link NebulaLocaleResolver} 语言来源优先级测试。
 */
class NebulaLocaleResolverTest {
    
    private static final Locale ZH_CN = Locale.SIMPLIFIED_CHINESE;
    
    private static final Locale EN_US = Locale.US;
    
    private NebulaLocaleResolver newResolver(boolean preferAccept) {
        NebulaI18nProperties properties = new NebulaI18nProperties();
        properties.setHeaderName("X-Lang");
        properties.setParamName("lang");
        properties.setPreferAcceptLanguage(preferAccept);
        return new NebulaLocaleResolver(properties);
    }
    
    @Test
    void headerWinsOverParamAndAcceptByDefault() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Lang", "en_US");
        request.addParameter("lang", "zh_CN");
        request.addHeader("Accept-Language", "zh_CN");
        assertThat(newResolver(false).resolveLocale(request)).isEqualTo(EN_US);
    }
    
    @Test
    void paramUsedWhenHeaderAbsent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("lang", "en_US");
        request.addHeader("Accept-Language", "zh_CN");
        assertThat(newResolver(false).resolveLocale(request)).isEqualTo(EN_US);
    }
    
    @Test
    void acceptLanguageUsedWhenHeaderAndParamAbsent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en_US,en;q=0.9");
        assertThat(newResolver(false).resolveLocale(request)).isEqualTo(EN_US);
    }
    
    @Test
    void preferAcceptLanguageMakesAcceptWin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Lang", "en_US");
        request.addHeader("Accept-Language", "zh_CN");
        assertThat(newResolver(true).resolveLocale(request)).isEqualTo(ZH_CN);
    }
    
    @Test
    void unsupportedLocaleFallsBackToDefault() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Lang", "fr_FR");
        assertThat(newResolver(false).resolveLocale(request)).isEqualTo(ZH_CN);
    }
    
    @Test
    void noHintUsesDefaultLocale() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(newResolver(false).resolveLocale(request)).isEqualTo(ZH_CN);
    }
    
}
