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
 
package com.nebula.log.logback.config;

import com.nebula.log.logback.desensitize.DesensitizeRule;
import com.nebula.log.logback.desensitize.DesensitizeRuntime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

/**
 * Applies {@link NebulaLogProperties#getDesensitize()} and custom {@link DesensitizeRule} beans
 * to {@link DesensitizeRuntime}.
 */
@RequiredArgsConstructor
public class DesensitizePropertiesBinder implements InitializingBean {
    
    private final NebulaLogProperties properties;
    private final ObjectProvider<DesensitizeRule> customRules;
    private final Environment environment;
    
    @Override
    public void afterPropertiesSet() {
        NebulaLogProperties.Desensitize desensitize = properties.getDesensitize();
        List<DesensitizeRule> extras = customRules.orderedStream().toList();
        DesensitizeRuntime.configure(
                desensitize.isEnabled() && !skippedByEnvironment(desensitize),
                Objects.requireNonNullElse(desensitize.getDisableRules(), List.of()),
                extras);
    }
    
    private boolean skippedByEnvironment(NebulaLogProperties.Desensitize desensitize) {
        List<String> disabled = Objects.requireNonNullElse(desensitize.getDisabledEnvironments(), List.of());
        if (disabled.isEmpty()) {
            return false;
        }
        for (String profile : environment.getActiveProfiles()) {
            if (disabled.contains(profile)) {
                return true;
            }
        }
        return false;
    }
}
