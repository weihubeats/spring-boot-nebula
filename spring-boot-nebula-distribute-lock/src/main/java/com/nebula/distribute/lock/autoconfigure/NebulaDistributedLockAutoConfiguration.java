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
 
package com.nebula.distribute.lock.autoconfigure;

import com.nebula.aop.base.NebulaBaseAnnotationAdvisor;
import com.nebula.distribute.lock.annotation.NebulaDistributedLock;
import com.nebula.distribute.lock.aop.NebulaDistributedLockAnnotationInterceptor;
import com.nebula.distribute.lock.core.NebulaDistributedLockTemplate;
import com.nebula.distribute.lock.core.RedissonDistributedLockTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.aop.Advisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author : wh
 * @date : 2024/3/15 13:39
 * @description:
 */
@Configuration(proxyBeanMethods = false)
public class NebulaDistributedLockAutoConfiguration {
    
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public RedissonDistributedLockTemplate redissonDistributedLockTemplate(RedissonClient redissonClient) {
        return new RedissonDistributedLockTemplate(redissonClient);
    }
    
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Advisor distributedLockAnnotationAdvisor(NebulaDistributedLockTemplate nebulaDistributedLockTemplate) {
        NebulaDistributedLockAnnotationInterceptor interceptor = new NebulaDistributedLockAnnotationInterceptor(nebulaDistributedLockTemplate);
        return new NebulaBaseAnnotationAdvisor(interceptor, NebulaDistributedLock.class);
    }
    
}
