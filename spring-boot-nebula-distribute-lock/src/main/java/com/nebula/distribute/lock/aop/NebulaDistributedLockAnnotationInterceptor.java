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
 
package com.nebula.distribute.lock.aop;

import com.nebula.distribute.lock.annotation.NebulaDistributedLock;
import com.nebula.distribute.lock.core.DistributedLock;
import com.nebula.distribute.lock.core.NebulaDistributedLockTemplate;
import com.nebula.distribute.lock.exception.DistributedLockException;
import com.nebula.web.common.utils.ExpressionUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StringUtils;

/**
 * @author : wh
 * @date : 2024/3/15 13:34
 * @description:
 */
@Slf4j
public class NebulaDistributedLockAnnotationInterceptor implements MethodInterceptor {
    
    private final NebulaDistributedLockTemplate lockTemplate;
    
    private final Cache<String, String> lockNameCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .build();
    
    private final Cache<String, String> elExpressionCache = Caffeine.newBuilder()
            .maximumSize(500)
            .build();
    
    public NebulaDistributedLockAnnotationInterceptor(NebulaDistributedLockTemplate lockTemplate) {
        if (lockTemplate == null) {
            throw new IllegalArgumentException("DistributedLockTemplate cannot be null");
        }
        this.lockTemplate = lockTemplate;
    }
    
    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        
        Method method = methodInvocation.getMethod();
        NebulaDistributedLock annotation = method.getAnnotation(NebulaDistributedLock.class);
        if (annotation == null) {
            return methodInvocation.proceed();
        }
        
        Object[] args = methodInvocation.getArguments();
        String lockName = getLockName(annotation, args, method);
        if (!StringUtils.hasText(lockName)) {
            throw new DistributedLockException("Lock name cannot be empty");
        }
        if (log.isDebugEnabled()) {
            log.debug("Using distributed lock: {}", lockName);
        }
        boolean fairLock = annotation.fairLock();
        boolean watchDogEnabled = annotation.watchDogEnabled();
        try {
            if (annotation.tryLock()) {
                return lockTemplate.tryLock(
                        createDistributedLock(methodInvocation, lockName),
                        annotation.tryWaitTime(),
                        annotation.outTime(),
                        annotation.timeUnit(),
                        fairLock,
                        watchDogEnabled);
            } else {
                return lockTemplate.lock(
                        createDistributedLock(methodInvocation, lockName),
                        annotation.outTime(),
                        annotation.timeUnit(),
                        fairLock,
                        watchDogEnabled);
            }
        } catch (DistributedLockException e) {
            log.error("Failed to acquire distributed lock: {}", lockName, e);
            throw e;
        }
        
    }
    
    /**
     * 创建分布式锁对象，保留原始异常类型
     */
    private DistributedLock<Object> createDistributedLock(MethodInvocation methodInvocation, String lockName) {
        return new DistributedLock<>() {
            
            @Override
            public Object process() {
                try {
                    return methodInvocation.proceed();
                } catch (DistributedLockException e) {
                    throw e;
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new DistributedLockException("Error executing locked method: " + lockName, e);
                }
            }
            
            @Override
            public String lockName() {
                return lockName;
            }
        };
        
    }
    
    /**
     * 获取锁名称
     */
    private String getLockName(NebulaDistributedLock annotation, Object[] args, Method method) {
        
        if (StringUtils.hasText(annotation.lockName())) {
            return annotation.lockName();
        }
        
        String lockNamePre = resolveLockPart(annotation.lockNamePre(), method, args);
        String lockNamePost = resolveLockPart(annotation.lockNamePost(), method, args);
        
        if (!StringUtils.hasText(lockNamePre) && !StringUtils.hasText(lockNamePost)) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder(64);
        if (StringUtils.hasText(lockNamePre)) {
            sb.append(lockNamePre);
        }
        if (StringUtils.hasText(lockNamePost)) {
            if (sb.length() > 0) {
                sb.append(annotation.separator());
            }
            sb.append(lockNamePost);
        }
        return sb.toString();
        
    }
    
    /**
     * 解析锁名的一部分（前缀或后缀）
     * - 非 EL 表达式：按 Method 缓存
     * - EL 表达式：缓存解析后的结果
     */
    private String resolveLockPart(String part, Method method, Object[] args) {
        if (!StringUtils.hasText(part)) {
            return null;
        }
        if (ExpressionUtil.isEl(part)) {
            String cached = elExpressionCache.getIfPresent(part);
            if (cached != null) {
                return cached;
            }
            String resolved = parseExpression(part, method, args);
            elExpressionCache.put(part, resolved);
            return resolved;
        }
        String cacheKey = method.toString() + "::" + part;
        return lockNameCache.get(cacheKey, k -> part);
    }
    
    private String parseExpression(String expression, Method method, Object[] args) {
        Object result = ExpressionUtil.parse(expression, method, args);
        return result != null ? result.toString() : "";
    }
}