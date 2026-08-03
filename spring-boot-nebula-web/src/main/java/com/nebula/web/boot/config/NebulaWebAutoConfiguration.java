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
 
package com.nebula.web.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebula.alert.feishu.FeiShuRoot;
import com.nebula.web.boot.error.DefaultNebulaErrorMonitor;
import com.nebula.web.boot.error.FeishuAlertLimiter;
import com.nebula.web.boot.error.NebulaAlertLimiter;
import com.nebula.web.boot.error.NebulaErrorMonitor;
import com.nebula.web.boot.error.RedisNebulaAlertLimiter;
import com.nebula.web.boot.annotation.NebulaResponseBodyAdvice;
import com.nebula.web.boot.filter.RepeatableReadFilter;
import java.time.Duration;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;

/**
 * @author : wh
 * @date : 2025/3/12 17:02
 * @description:
 */
@EnableConfigurationProperties(NebulaWebProperties.class)
@Configuration
public class NebulaWebAutoConfiguration {
    
    @Bean
    public BaseWebMvcConfig baseWebMvcConfig() {
        return new BaseWebMvcConfig();
        
    }
    
    @Bean
    public NebulaResponseBodyAdvice nebulaResponseBodyAdvice(NebulaWebProperties nebulaWebProperties,
                                                             ObjectMapper objectMapper) {
        return new NebulaResponseBodyAdvice(nebulaWebProperties, objectMapper);
    }
    
    @Bean
    public FilterRegistrationBean<RepeatableReadFilter> repeatableReadFilterRegistration() {
        FilterRegistrationBean<RepeatableReadFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableReadFilter());
        // 拦截所有路径
        registration.addUrlPatterns("/*");
        registration.setName("nebulaRepeatableReadFilter");
        // 关键点：设置高优先级。
        // HIGHEST_PRECEDENCE 留给 Spring 的编码过滤器等底层框架使用，我们排在紧接着的位置即可
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
    
    @ConditionalOnProperty(name = "nebula.web.monitor.type", havingValue = "feishu")
    @Bean
    public NebulaAlertLimiter feishuAlertLimiter(NebulaWebProperties nebulaWebProperties) {
        return new FeishuAlertLimiter(nebulaWebProperties.getMonitorLimitWindowSeconds(),
                nebulaWebProperties.getMonitorLimitMaxCount());
    }
    
    @ConditionalOnProperty(name = "nebula.web.monitor.type", havingValue = "feishu")
    @ConditionalOnBean(RedissonClient.class)
    @Primary
    @Bean
    public NebulaAlertLimiter redisAlertLimiter(RedissonClient redissonClient,
                                                NebulaWebProperties nebulaWebProperties) {
        return new RedisNebulaAlertLimiter(redissonClient,
                Duration.ofSeconds(nebulaWebProperties.getMonitorLimitWindowSeconds()),
                Duration.ofSeconds(nebulaWebProperties.getMonitorLimitExpireSeconds()),
                nebulaWebProperties.getMonitorLimitMaxCount(),
                nebulaWebProperties.getMonitorLimitKeyPrefix());
    }
    
    @ConditionalOnProperty(name = "nebula.web.monitor.type", havingValue = "feishu")
    @Bean
    public NebulaErrorMonitor defaultNebulaErrorMonitor(FeiShuRoot feiShuRoot,
                                                        NebulaWebProperties nebulaWebProperties,
                                                        NebulaAlertLimiter alertLimiter) {
        return new DefaultNebulaErrorMonitor(feiShuRoot, nebulaWebProperties, alertLimiter);
    }
}
