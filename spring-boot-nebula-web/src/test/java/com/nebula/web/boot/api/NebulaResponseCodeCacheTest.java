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
 
package com.nebula.web.boot.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.nebula.web.boot.config.NebulaWebProperties;
import com.nebula.web.common.utils.SpringBeanUtils;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link NebulaResponse} code 解析缓存行为测试：缓存前后行为一致，且无上下文时走默认 int。
 */
class NebulaResponseCodeCacheTest {
    
    private static AnnotationConfigApplicationContext context;
    
    @BeforeAll
    static void setUp() {
        NebulaResponse.resetCodeResolverForTest();
        context = new AnnotationConfigApplicationContext(CodeConfig.class);
        new SpringBeanUtils().setApplicationContext(context);
    }
    
    @AfterAll
    static void tearDown() {
        new SpringBeanUtils().setApplicationContext(null);
        NebulaResponse.resetCodeResolverForTest();
        if (context != null) {
            context.close();
        }
    }
    
    @Test
    void resolvesMappingFromContextAndStaysConsistentAfterCache() {
        // 上下文中配置了 SUCCESS -> "Success"，非 SUCCESS 保持 int
        assertThat(NebulaResponse.toWireCode(200)).isEqualTo("Success");
        assertThat(NebulaResponse.toWireCode(500)).isEqualTo(500);
        
        // 缓存后行为保持一致（含上下文被移除后仍使用已解析的配置）
        new SpringBeanUtils().setApplicationContext(null);
        assertThat(NebulaResponse.toWireCode(200)).isEqualTo("Success");
        assertThat(NebulaResponse.matchesCode(200, "Success")).isTrue();
        assertThat(NebulaResponse.matchesCode(200, 200)).isTrue();
        assertThat(NebulaResponse.matchesCode(200, "Other")).isFalse();
        
        NebulaResponse<String> response = NebulaResponse.data("ok", "success");
        assertThat(response.getCode()).isEqualTo("Success");
        assertThat(NebulaResponse.isSuccess(response)).isTrue();
        assertThat(NebulaResponse.isSuccess(null)).isFalse();
    }
    
    @Configuration
    static class CodeConfig {
        
        @Bean
        NebulaWebProperties nebulaWebProperties() {
            NebulaWebProperties properties = new NebulaWebProperties();
            properties.setCodeMapping(Map.of(200, "Success"));
            return properties;
        }
    }
}
