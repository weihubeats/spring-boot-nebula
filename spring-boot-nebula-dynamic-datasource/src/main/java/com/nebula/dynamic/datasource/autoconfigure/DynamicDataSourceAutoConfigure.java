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
 
package com.nebula.dynamic.datasource.autoconfigure;

import com.nebula.dynamic.datasource.annotation.NebulaDS;
import com.nebula.dynamic.datasource.core.DynamicDataSourceMethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
public class DynamicDataSourceAutoConfigure {
    
    @Bean
    public Advisor dynamicDataSourceAdvisor() {
        AnnotationMatchingPointcut classPointcut = new AnnotationMatchingPointcut(NebulaDS.class, true);
        Pointcut methodPointcut = AnnotationMatchingPointcut.forMethodAnnotation(NebulaDS.class);
        Pointcut union = new ComposablePointcut(classPointcut).union(methodPointcut);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(union, new DynamicDataSourceMethodInterceptor());
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return advisor;
    }
}
