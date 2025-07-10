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
 
package com.nebula.base.utils.juc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author : wh
 * @date : 2025/6/27
 * @description:
 */
class ThreadPoolBuilderTest {
    
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    
    @Test
    @DisplayName("CPU密集型 - 测试默认配置")
    void testCPUBuilder_DefaultSettings() {
        ThreadPoolExecutor executor = ThreadPoolBuilder.cpuBoundBuilder().build();
        
        assertEquals(CPU_COUNT, executor.getCorePoolSize(), "默认核心线程数应为CPU核心数");
        assertEquals(CPU_COUNT * 2, executor.getMaximumPoolSize(), "默认最大线程数应为CPU核心数的2倍");
        assertEquals(60L, executor.getKeepAliveTime(TimeUnit.SECONDS), "默认KeepAlive时间应为60秒");
        assertTrue(executor.getQueue() instanceof LinkedBlockingQueue, "默认应使用无界队列LinkedBlockingQueue");
        assertTrue(executor.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy, "默认拒绝策略应为AbortPolicy");
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("CPU密集型 - 测试自定义配置")
    void testCPUBuilder_CustomSettings() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final String expectedPrefix = "my-cpu-pool";
        
        ThreadPoolExecutor executor = ThreadPoolBuilder.cpuBoundBuilder()
                .setThreadNamePrefix(expectedPrefix)
                .setDaemon(true)
                .setQueueSize(100)
                .setMaximumPoolSize(50)
                .setKeepAliveTime(2, TimeUnit.MINUTES)
                .setRejectHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .build();
        
        // 验证配置
        assertEquals(CPU_COUNT, executor.getCorePoolSize());
        assertEquals(50, executor.getMaximumPoolSize());
        assertEquals(120L, executor.getKeepAliveTime(TimeUnit.SECONDS));
        assertTrue(executor.getQueue() instanceof ArrayBlockingQueue);
        assertEquals(100, executor.getQueue().remainingCapacity());
        assertTrue(executor.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.CallerRunsPolicy);
        
        // 验证线程工厂
        executor.submit(() -> {
            assertTrue(Thread.currentThread().getName().startsWith(expectedPrefix), "线程名称前缀不匹配");
            assertTrue(Thread.currentThread().isDaemon(), "线程应为守护线程");
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS), "任务未在规定时间内执行");
        executor.shutdown();
    }
    
    @Test
    @DisplayName("IO密集型 - 测试核心线程数计算")
    void testIOBuilder_CorePoolSizeCalculation() {
        // 假设 waitTime=3, computeTime=1 -> coreSize = CPU * (1 + 3/1) = 4 * CPU
        ThreadPoolExecutor executor = ThreadPoolBuilder.ioBoundBuilder().build(3, 1);
        int expectedCoreSize = CPU_COUNT * 4;
        assertEquals(expectedCoreSize, executor.getCorePoolSize(), "IO密集型核心线程数计算不正确");
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("IO密集型 - 测试computeTime为0时抛出异常")
    void testIOBuilder_ComputeTimeIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> ThreadPoolBuilder.ioBoundBuilder().build(10, 0),
                "当computeTime为0时应抛出IllegalArgumentException");
    }
    
    @Test
    @DisplayName("IO密集型 - 测试自定义配置")
    void testIOBuilder_CustomSettings() {
        ThreadPoolExecutor executor = ThreadPoolBuilder.ioBoundBuilder()
                .setMaximumPoolSize(100)
                .setQueueSize(50)
                .build(1, 1);
        
        int expectedCoreSize = CPU_COUNT * 2;
        assertEquals(expectedCoreSize, executor.getCorePoolSize());
        assertEquals(100, executor.getMaximumPoolSize());
        assertTrue(executor.getQueue() instanceof ArrayBlockingQueue);
        assertEquals(50, executor.getQueue().remainingCapacity());
        
        executor.shutdown();
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}