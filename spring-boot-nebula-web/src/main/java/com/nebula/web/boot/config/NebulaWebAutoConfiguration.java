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
import com.nebula.web.boot.monitor.DefaultNebulaErrorMonitor;
import com.nebula.web.boot.monitor.FeishuAlertChannel;
import com.nebula.web.boot.monitor.LocalAlertLimiter;
import com.nebula.web.boot.monitor.NebulaAlertChannel;
import com.nebula.web.boot.monitor.NebulaAlertLimiter;
import com.nebula.web.boot.monitor.NebulaErrorMonitor;
import com.nebula.web.boot.monitor.RedisAlertLimiter;
import com.nebula.web.boot.annotation.NebulaResponseBodyAdvice;
import com.nebula.web.boot.filter.RepeatableReadFilter;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    @Configuration(proxyBeanMethods = false)
    static class FeishuAlertConfiguration {
        
        @Bean
        @ConditionalOnMissingBean(NebulaAlertChannel.class)
        public NebulaAlertChannel feishuAlertChannel(FeiShuRoot feiShuRoot, NebulaWebProperties nebulaWebProperties) {
            return new FeishuAlertChannel(feiShuRoot, nebulaWebProperties.getMonitor().getUrl());
        }
        
        @Bean
        @ConditionalOnProperty(name = "nebula.web.monitor.limit.storage", havingValue = "local", matchIfMissing = true)
        @ConditionalOnMissingBean(NebulaAlertLimiter.class)
        public NebulaAlertLimiter localAlertLimiter(NebulaWebProperties nebulaWebProperties) {
            return new LocalAlertLimiter(nebulaWebProperties.getMonitor().getLimit().getWindowSeconds(),
                    nebulaWebProperties.getMonitor().getLimit().getMaxCount());
        }
    }
    
    @ConditionalOnProperty(name = "nebula.web.monitor.limit.storage", havingValue = "redis")
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    @Configuration(proxyBeanMethods = false)
    static class RedisAlertLimiterConfiguration {
        
        @Bean
        @ConditionalOnMissingBean(NebulaAlertLimiter.class)
        public NebulaAlertLimiter redisAlertLimiter(org.redisson.api.RedissonClient redissonClient,
                                                    NebulaWebProperties nebulaWebProperties) {
            return new RedisAlertLimiter(redissonClient,
                    Duration.ofSeconds(nebulaWebProperties.getMonitor().getLimit().getWindowSeconds()),
                    Duration.ofSeconds(nebulaWebProperties.getMonitor().getLimit().getExpireSeconds()),
                    nebulaWebProperties.getMonitor().getLimit().getMaxCount(),
                    nebulaWebProperties.getMonitor().getLimit().getKeyPrefix());
        }
    }
    
    @ConditionalOnProperty(name = "nebula.web.monitor.type")
    @ConditionalOnMissingBean(NebulaErrorMonitor.class)
    @Bean
    public NebulaErrorMonitor defaultNebulaErrorMonitor(NebulaWebProperties nebulaWebProperties,
                                                        NebulaAlertLimiter alertLimiter,
                                                        NebulaAlertChannel alertChannel) {
        return new DefaultNebulaErrorMonitor(nebulaWebProperties, alertLimiter, alertChannel);
    }
}
