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
 
package com.nebula.aggregate.annotation;

import com.nebula.aggregate.core.AbstractOldObj;
import com.nebula.base.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author : wh
 * @date : 2023/12/18 19:57
 * @description:
 */
@Aspect
@Slf4j
@Component
public class CreateOldObjAspect {
    
    @AfterReturning(pointcut = "@annotation(com.nebula.aggregate.annotation.AggregateCreate) || " +
            "@annotation(com.nebula.aggregate.annotation.CreateOldObj)", returning = "returnVal")
    public void handleRequestMethod(JoinPoint pjp, Object returnVal) {
        if (returnVal instanceof AbstractOldObj) {
            ((AbstractOldObj) returnVal).setOldObject(copy(returnVal));
        }
    }
    
    public Object copy(Object oldObject) {
        return JsonUtil.json2JavaBean(JsonUtil.toJSONString(oldObject), oldObject.getClass());
    }
    
}
