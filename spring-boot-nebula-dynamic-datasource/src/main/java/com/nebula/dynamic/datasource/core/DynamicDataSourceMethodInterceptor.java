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
 
package com.nebula.dynamic.datasource.core;

import com.nebula.dynamic.datasource.annotation.NebulaDS;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
public class DynamicDataSourceMethodInterceptor implements MethodInterceptor {
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        NebulaDS ds = getDSAnnotation(invocation);
        boolean pushed = false;
        if (ds != null) {
            DynamicDataSourceContextHolder.setDataSource(ds.value());
            pushed = true;
        }
        try {
            return invocation.proceed();
        } finally {
            if (pushed) {
                DynamicDataSourceContextHolder.clear();
            }
        }
        
    }
    
    private NebulaDS getDSAnnotation(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        NebulaDS ds = AnnotatedElementUtils.findMergedAnnotation(method, NebulaDS.class);
        if (ds == null) {
            Class<?> targetClass = AopUtils.getTargetClass(invocation.getThis());
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            ds = AnnotatedElementUtils.findMergedAnnotation(specificMethod, NebulaDS.class);
            if (ds == null) {
                ds = AnnotatedElementUtils.findMergedAnnotation(targetClass, NebulaDS.class);
            }
        }
        return ds;
    }
    
}