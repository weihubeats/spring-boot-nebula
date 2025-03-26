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
import com.nebula.web.boot.error.NebulaErrorMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wh
 * @date : 2025/3/12 17:02
 * @description:
 */
@EnableConfigurationProperties(NebulaWebProperties.class)
@Configuration
public class NebulaWebAutoConfiguration {
    
    @Bean
    public BaseWebMvcConfig baseWebMvcConfig(ObjectMapper objectMapper, NebulaWebProperties webProperties) {
        return new BaseWebMvcConfig(objectMapper, webProperties);
        
    }
    
    @ConditionalOnProperty(name = "nebula.web.monitor.type", havingValue = "feishu")
    @Bean
    public NebulaErrorMonitor defaultNebulaErrorMonitor(FeiShuRoot feiShuRoot,
                                                        NebulaWebProperties nebulaWebProperties) {
        return new DefaultNebulaErrorMonitor(feiShuRoot, nebulaWebProperties);
        
    }
}
