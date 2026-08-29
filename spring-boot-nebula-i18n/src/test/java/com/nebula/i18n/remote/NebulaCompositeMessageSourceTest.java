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

import static org.assertj.core.api.Assertions.assertThat;

import com.nebula.i18n.core.NebulaCompositeMessageSource;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 远程优先、本地兜底组合测试。
 */
class NebulaCompositeMessageSourceTest {
    
    private final NebulaCompositeMessageSource composite = buildComposite();
    
    private NebulaCompositeMessageSource buildComposite() {
        Properties remoteZh = new Properties();
        remoteZh.setProperty("greeting", "远程你好 {0}");
        remoteZh.setProperty("remote.only", "远程独有");
        
        RemoteMessageLoader loader = () -> {
            Map<Locale, Properties> map = new HashMap<>();
            map.put(Locale.SIMPLIFIED_CHINESE, remoteZh);
            return map;
        };
        RemoteMessageSource remote = new RemoteMessageSource(loader);
        remote.refresh();
        
        ResourceBundleMessageSource local = new ResourceBundleMessageSource();
        local.setBasename("test/messages");
        local.setDefaultEncoding("UTF-8");
        local.setUseCodeAsDefaultMessage(false);
        return new NebulaCompositeMessageSource(remote, local);
    }
    
    @Test
    void remoteOverridesLocal() {
        assertThat(composite.getMessage("greeting", new Object[]{"nebula"}, Locale.SIMPLIFIED_CHINESE))
                .isEqualTo("远程你好 nebula");
    }
    
    @Test
    void remoteOnlyKeyResolved() {
        assertThat(composite.getMessage("remote.only", null, Locale.SIMPLIFIED_CHINESE))
                .isEqualTo("远程独有");
    }
    
    @Test
    void fallsBackToLocalWhenRemoteMisses() {
        // local.only 仅存在于本地资源 test/messages_zh_CN.properties
        assertThat(composite.getMessage("local.only", null, Locale.SIMPLIFIED_CHINESE))
                .isEqualTo("本地独有");
    }
    
    @Test
    void unknownKeyUsesDefaultMessage() {
        assertThat(composite.getMessage("no.such.key", null, "default-text", Locale.SIMPLIFIED_CHINESE))
                .isEqualTo("default-text");
    }
    
}
