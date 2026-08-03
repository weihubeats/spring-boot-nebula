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
 
package com.nebula.feign.autoconfigure;

import com.nebula.feign.codec.NebulaFeignDecoder;
import com.nebula.feign.codec.NebulaFeignErrorDecoder;
import com.nebula.feign.config.NebulaFeignProperties;
import com.nebula.feign.log.NebulaFeignLogCapability;
import feign.Capability;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;

/**
 * 注册 Nebula Feign Decoder / ErrorDecoder / 日志过滤器。
 */
@AutoConfiguration
@ConditionalOnClass(name = "feign.Feign")
@EnableConfigurationProperties(NebulaFeignProperties.class)
public class NebulaFeignAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
                                ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        return new OptionalDecoder(
                new ResponseEntityDecoder(
                        new NebulaFeignDecoder(new SpringDecoder(messageConverters, customizers))));
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder feignErrorDecoder() {
        return new NebulaFeignErrorDecoder();
    }
    
    /**
     * 统一打印 Feign 请求参数、响应与耗时，并支持慢调用告警。
     */
    @Bean
    @ConditionalOnMissingBean(NebulaFeignLogCapability.class)
    @ConditionalOnProperty(prefix = "nebula.feign.log", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Capability nebulaFeignLogCapability(NebulaFeignProperties properties) {
        return new NebulaFeignLogCapability(properties);
    }
}
