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

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.nebula.log.logback.desensitize.DesensitizeRuntime;
import com.nebula.log.logback.desensitize.rule.MobileDesensitizeRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NebulaLogAutoConfigurationTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NebulaLogAutoConfiguration.class));
    
    @AfterEach
    void resetRuntime() {
        DesensitizeRuntime.reset();
    }
    
    @Test
    void doesNotRegisterWhenDisabled() {
        contextRunner
                .withPropertyValues("nebula.log.feishu.webhook-url=https://example.com/hook")
                .run(context -> assertThat(context).doesNotHaveBean(FeishuErrorAppenderLifecycle.class));
    }
    
    @Test
    void registersAppenderWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "nebula.log.feishu.enabled=true",
                        "nebula.log.feishu.webhook-url=https://example.com/hook",
                        "nebula.log.feishu.title=ut-app")
                .run(context -> {
                    assertThat(context).hasSingleBean(FeishuErrorAppenderLifecycle.class);
                    FeishuErrorAppenderLifecycle lifecycle = context.getBean(FeishuErrorAppenderLifecycle.class);
                    assertThat(lifecycle.isRunning()).isTrue();
                    assertThat(lifecycle.getAppender()).isNotNull();
                    assertThat(lifecycle.getAppender().isStarted()).isTrue();
                    
                    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
                    assertThat(root.getAppender(FeishuErrorAppenderLifecycle.APPENDER_NAME)).isNotNull();
                });
    }
    
    @Test
    void bindsDesensitizeDisableRules() {
        contextRunner
                .withPropertyValues("nebula.log.desensitize.disable-rules=mobile,email")
                .run(context -> {
                    assertThat(context).hasSingleBean(DesensitizePropertiesBinder.class);
                    assertThat(DesensitizeRuntime.enabled()).isTrue();
                    assertThat(DesensitizeRuntime.desensitizer().rules())
                            .noneMatch(r -> MobileDesensitizeRule.NAME.equals(r.name()));
                    assertThat(DesensitizeRuntime.apply("13812348000")).isEqualTo("13812348000");
                    assertThat(DesensitizeRuntime.apply("password=secret")).isEqualTo("password=***");
                });
    }
    
    @Test
    void bindsDesensitizeDisabled() {
        contextRunner
                .withPropertyValues("nebula.log.desensitize.enabled=false")
                .run(context -> {
                    assertThat(DesensitizeRuntime.enabled()).isFalse();
                    assertThat(DesensitizeRuntime.apply("13812348000")).isEqualTo("13812348000");
                });
    }
    
    @Test
    void desensitizesByDefaultInDevProfile() {
        contextRunner
                .withPropertyValues("spring.profiles.active=dev")
                .run(context -> {
                    assertThat(DesensitizeRuntime.enabled()).isTrue();
                    assertThat(DesensitizeRuntime.apply("13812348000")).isEqualTo("138****8000");
                });
    }
    
    @Test
    void desensitizesInDevProfileWithExplicitEmptyList() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=dev",
                        "nebula.log.desensitize.disabled-environments=")
                .run(context -> {
                    NebulaLogProperties properties = context.getBean(NebulaLogProperties.class);
                    assertThat(properties.getDesensitize().getDisabledEnvironments()).isEmpty();
                    assertThat(DesensitizeRuntime.enabled()).isTrue();
                    assertThat(DesensitizeRuntime.apply("13812348000")).isEqualTo("138****8000");
                });
    }
    
    @Test
    void skipsDesensitizeInCustomDisabledEnvironments() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=local",
                        "nebula.log.desensitize.disabled-environments=local")
                .run(context -> {
                    assertThat(DesensitizeRuntime.enabled()).isFalse();
                    assertThat(DesensitizeRuntime.apply("13812348000")).isEqualTo("13812348000");
                });
    }
    
    @Test
    void desensitizesInProductionProfile() {
        contextRunner
                .withPropertyValues("spring.profiles.active=prod")
                .run(context -> {
                    assertThat(DesensitizeRuntime.enabled()).isTrue();
                    assertThat(DesensitizeRuntime.apply("13812348000")).isEqualTo("138****8000");
                });
    }
}
