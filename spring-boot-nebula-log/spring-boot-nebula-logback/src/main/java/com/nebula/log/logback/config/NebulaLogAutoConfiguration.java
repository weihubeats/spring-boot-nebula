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

import ch.qos.logback.classic.LoggerContext;
import com.nebula.log.logback.desensitize.DesensitizeRule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Auto-configures logback extensions from {@link NebulaLogProperties}.
 */
@AutoConfiguration
@ConditionalOnClass(LoggerContext.class)
@EnableConfigurationProperties(NebulaLogProperties.class)
public class NebulaLogAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DesensitizePropertiesBinder desensitizePropertiesBinder(
                                                                   NebulaLogProperties properties, ObjectProvider<DesensitizeRule> customRules,
                                                                   Environment environment) {
        return new DesensitizePropertiesBinder(properties, customRules, environment);
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "nebula.log.feishu", name = "enabled", havingValue = "true")
    public FeishuErrorAppenderLifecycle feishuErrorAppenderLifecycle(NebulaLogProperties properties) {
        return new FeishuErrorAppenderLifecycle(properties);
    }
}
