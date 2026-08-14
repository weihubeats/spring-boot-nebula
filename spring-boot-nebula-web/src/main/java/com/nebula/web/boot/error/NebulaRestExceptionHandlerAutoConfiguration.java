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
 
package com.nebula.web.boot.error;

import com.nebula.web.boot.config.NebulaWebAutoConfiguration;
import com.nebula.web.boot.config.NebulaWebProperties;
import com.nebula.web.boot.monitor.NebulaErrorMonitor;
import com.nebula.web.common.utils.NebulaSysWebUtils;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 按条件注册 {@link NebulaRestExceptionHandler}。
 *
 * <p>条件放在普通 {@code @Configuration} 类上（而非 handler 类自身），避免
 * {@code @ConditionalOnMissingBean(annotation = RestControllerAdvice.class)} 匹配到 handler
 * 自己的 bean 定义导致永不注册。同时 {@code @AutoConfigureBefore(NebulaWebAutoConfiguration)} 保证
 * 评估时 SDK 自家 {@code NebulaResponseBodyAdvice} 尚未注册，条件只对项目自定义 advice 生效。
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(NebulaWebAutoConfiguration.class)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "nebula.web.exception-handler", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(annotation = RestControllerAdvice.class)
public class NebulaRestExceptionHandlerAutoConfiguration {
    
    @Bean
    public NebulaRestExceptionHandler nebulaRestExceptionHandler(NebulaSysWebUtils nebulaSysWebUtils,
                                                                 NebulaWebProperties nebulaWebProperties,
                                                                 ObjectProvider<NebulaErrorMonitor> nebulaErrorMonitor) {
        return new NebulaRestExceptionHandler(nebulaSysWebUtils, nebulaWebProperties, nebulaErrorMonitor.getIfAvailable());
    }
}
