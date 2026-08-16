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
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NebulaDistributedLockAnnotationInterceptorTest {
    
    @Mock
    private NebulaDistributedLockTemplate lockTemplate;
    
    private NebulaDistributedLockAnnotationInterceptor interceptor;
    private TestService testService;
    
    @BeforeEach
    void setUp() {
        interceptor = new NebulaDistributedLockAnnotationInterceptor(lockTemplate);
        testService = new TestService();
    }
    
    @Test
    void passesThroughWhenNoAnnotation() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("noLockMethod", String.class), new Object[]{"test"});
        
        Object result = interceptor.invoke(invocation);
        
        assertEquals("proceed-result", result);
        verify(invocation).proceed();
    }
    
    @Test
    void usesStaticLockName() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("staticLockMethod", String.class), new Object[]{"arg1"});
        when(lockTemplate.lock(any(DistributedLock.class), eq(20L), eq(TimeUnit.SECONDS), eq(false), eq(true)))
                .thenReturn("result");
        
        Object result = interceptor.invoke(invocation);
        
        assertEquals("result", result);
        verify(lockTemplate).lock(any(DistributedLock.class), eq(20L), eq(TimeUnit.SECONDS), eq(false), eq(true));
    }
    
    @Test
    void usesPrefixAndSuffix() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("prefixSuffixMethod"), new Object[]{});
        lenient().doAnswer(invocationOnMock -> {
            DistributedLock<?> lock = invocationOnMock.getArgument(0);
            String name = lock.lockName();
            assertEquals("prefix_suffix", name);
            return "ok";
        }).when(lockTemplate).lock(any(DistributedLock.class), anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
        
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(any(DistributedLock.class), anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
    }
    
    @Test
    void throwsWhenLockNameIsEmpty() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("emptyLockNameMethod"), new Object[]{});
        
        DistributedLockException e = assertThrows(DistributedLockException.class, () -> interceptor.invoke(invocation));
        assertTrue(e.getMessage().contains("cannot be empty"));
    }
    
    @Test
    void propagatesRuntimeExceptionFromProcess() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("runtimeExceptionMethod"), new Object[]{});
        lenient().when(invocation.proceed()).thenThrow(new RuntimeException("original error"));
        lenient().doAnswer(invocationOnMock -> {
            DistributedLock<?> lock = invocationOnMock.getArgument(0);
            return lock.process();
        }).when(lockTemplate).lock(any(DistributedLock.class), anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
        
        RuntimeException e = assertThrows(RuntimeException.class, () -> interceptor.invoke(invocation));
        assertEquals("original error", e.getMessage());
    }
    
    @Test
    void tryLockModeCallsTryLock() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("tryLockMethod"), new Object[]{});
        when(lockTemplate.tryLock(any(DistributedLock.class), eq(30L), eq(20L), eq(TimeUnit.SECONDS), eq(false), eq(true)))
                .thenReturn("result");
        
        Object result = interceptor.invoke(invocation);
        
        assertEquals("result", result);
        verify(lockTemplate).tryLock(any(DistributedLock.class), eq(30L), eq(20L), eq(TimeUnit.SECONDS), eq(false), eq(true));
    }
    
    @Test
    void fairLockModeSetsFairFlag() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("fairLockMethod"), new Object[]{});
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(any(DistributedLock.class), anyLong(), any(TimeUnit.class), eq(true), anyBoolean());
    }
    
    @Test
    void watchDogDisabledMode() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("watchDogDisabledMethod"), new Object[]{});
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(any(DistributedLock.class), anyLong(), any(TimeUnit.class), anyBoolean(), eq(false));
    }
    
    @Test
    void rethrowsDistributedLockException() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("staticLockMethod", String.class), new Object[]{"arg1"});
        when(lockTemplate.lock(any(DistributedLock.class), anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean()))
                .thenThrow(new DistributedLockException("lock acquire failed"));
        
        DistributedLockException e = assertThrows(DistributedLockException.class, () -> interceptor.invoke(invocation));
        assertEquals("lock acquire failed", e.getMessage());
    }
    
    @Test
    void lockNameContainsCorrectValue() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("staticLockMethod", String.class), new Object[]{"arg1"});
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(argThat(lock -> "order-create".equals(lock.lockName())),
                anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
    }
    
    @Test
    void lockNameWithOnlyPrefix() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("onlyPrefixMethod"), new Object[]{});
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(argThat(lock -> {
            String name = lock.lockName();
            return StringUtils.hasText(name) && name.startsWith("prefix");
        }),
                anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
    }
    
    @Test
    void lockNameWithOnlySuffix() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("onlySuffixMethod"), new Object[]{});
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(argThat(lock -> {
            String name = lock.lockName();
            return StringUtils.hasText(name) && "suffix".equals(name);
        }),
                anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
    }
    
    @Test
    void customTimeUnit() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("customTimeUnitMethod"), new Object[]{});
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(any(DistributedLock.class), eq(10L), eq(TimeUnit.MILLISECONDS), anyBoolean(), anyBoolean());
    }
    
    @Test
    void customSeparator() throws Throwable {
        MethodInvocation invocation = mockMethodInvocation(TestService.class.getMethod("customSeparatorMethod"), new Object[]{});
        
        interceptor.invoke(invocation);
        
        verify(lockTemplate).lock(argThat(lock -> lock.lockName().contains("_custom_sep_")),
                anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
    }
    
    @Test
    void spelLockNameResolvedPerInvocation() throws Throwable {
        // 回归测试：SpEL 解析结果曾按表达式字符串缓存，导致所有调用复用第一次的锁名
        MethodInvocation first = mockMethodInvocation(TestService.class.getMethod("spelLockMethod", String.class), new Object[]{"100"});
        MethodInvocation second = mockMethodInvocation(TestService.class.getMethod("spelLockMethod", String.class), new Object[]{"200"});
        
        interceptor.invoke(first);
        interceptor.invoke(second);
        
        verify(lockTemplate).lock(argThat(lock -> "order_100".equals(lock.lockName())),
                anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
        verify(lockTemplate).lock(argThat(lock -> "order_200".equals(lock.lockName())),
                anyLong(), any(TimeUnit.class), anyBoolean(), anyBoolean());
    }
    
    // --- Helper ---
    
    private MethodInvocation mockMethodInvocation(Method method, Object[] args) throws Throwable {
        MethodInvocation invocation = mock(MethodInvocation.class);
        lenient().when(invocation.getMethod()).thenReturn(method);
        lenient().when(invocation.getArguments()).thenReturn(args);
        lenient().when(invocation.proceed()).thenReturn("proceed-result");
        return invocation;
    }
    
    // --- Test service with annotated methods ---
    
    private static class TestService {
        
        public String noLockMethod(String arg) {
            return "ok";
        }
        
        @NebulaDistributedLock(lockName = "order-create")
        public String staticLockMethod(String orderId) {
            return "static";
        }
        
        @NebulaDistributedLock(lockNamePre = "prefix", lockNamePost = "suffix")
        public String prefixSuffixMethod() {
            return "pre-suffix";
        }
        
        @NebulaDistributedLock()
        public String emptyLockNameMethod() {
            return "empty";
        }
        
        @NebulaDistributedLock(lockName = "test")
        public String runtimeExceptionMethod() throws RuntimeException {
            throw new RuntimeException("original error");
        }
        
        @NebulaDistributedLock(lockName = "test", tryLock = true)
        public String tryLockMethod() {
            return "tryLock";
        }
        
        @NebulaDistributedLock(lockName = "test", fairLock = true)
        public String fairLockMethod() {
            return "fair";
        }
        
        @NebulaDistributedLock(lockName = "test", watchDogEnabled = false)
        public String watchDogDisabledMethod() {
            return "no-watchdog";
        }
        
        @NebulaDistributedLock(lockNamePre = "prefix")
        public String onlyPrefixMethod() {
            return "only-prefix";
        }
        
        @NebulaDistributedLock(lockNamePost = "suffix")
        public String onlySuffixMethod() {
            return "only-suffix";
        }
        
        @NebulaDistributedLock(lockName = "test", outTime = 10, timeUnit = TimeUnit.MILLISECONDS)
        public String customTimeUnitMethod() {
            return "custom-unit";
        }
        
        @NebulaDistributedLock(lockNamePre = "order", lockNamePost = "#orderId", separator = "_")
        public String spelLockMethod(String orderId) {
            return "spel";
        }
        
        @NebulaDistributedLock(lockNamePre = "a", lockNamePost = "b", separator = "_custom_sep_")
        public String customSeparatorMethod() {
            return "custom-sep";
        }
    }
}
