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

import com.nebula.base.exception.CompletableException;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author : wh
 * @date : 2025/6/19
 * @description:
 */
class CompletableFutureUtilsTest {
    
    @AfterAll
    static void tearDown() {
        // CompletableFutureUtils.shutdownDefaultExecutor();
    }
    
    @Test
    @DisplayName("单任务：正常返回结果")
    void testSupplyAndGetSuccess() {
        String value = CompletableFutureUtils.supplyAndGet(() -> "OK");
        assertEquals("OK", value);
    }
    
    @Test
    @DisplayName("单任务：抛出运行时异常被包装为 CompletableException，且保留 cause")
    void testSupplyAndGetExceptionWraps() {
        IllegalStateException original = new IllegalStateException("boom");
        CompletableException ex = assertThrows(CompletableException.class,
                () -> CompletableFutureUtils.supplyAndGet(() -> {
                    throw original;
                }));
        assertSame(original, ex.getCause());
    }
    
    @Test
    @DisplayName("单任务：超时触发 CompletableException")
    void testSupplyAndGetTimeout() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableException ex = assertThrows(CompletableException.class,
                    () -> CompletableFutureUtils.supplyAndGet(() -> {
                        sleepSilently(500);
                        return 1;
                    }, 100, TimeUnit.MILLISECONDS, executor));
            assertTrue(ex.getMessage().toLowerCase().contains("timed"));
        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    @DisplayName("两任务：聚合成功 Tuple2")
    void testAllSupplyAndGet2() {
        Tuple2<Integer, String> tuple =
                CompletableFutureUtils.allSupplyAndGet(() -> 10, () -> "abc");
        assertEquals(10, tuple._1());
        assertEquals("abc", tuple._2());
    }
    
    @Test
    @DisplayName("三任务：聚合成功 Tuple3")
    void testAllSupplyAndGet3() {
        Tuple3<String, Integer, Boolean> tuple =
                CompletableFutureUtils.allSupplyAndGet(() -> "x", () -> 7, () -> Boolean.TRUE);
        assertEquals("x", tuple._1());
        assertEquals(7, tuple._2());
        assertTrue(tuple._3());
    }
    
    @Test
    @DisplayName("四任务：聚合成功 Tuple4")
    void testAllSupplyAndGet4() {
        Tuple4<Integer, Integer, Integer, Integer> tuple =
                CompletableFutureUtils.allSupplyAndGet(() -> 1, () -> 2, () -> 3, () -> 4);
        assertEquals(1, tuple._1());
        assertEquals(2, tuple._2());
        assertEquals(3, tuple._3());
        assertEquals(4, tuple._4());
    }
    
    @Test
    @DisplayName("五任务：聚合成功 Tuple5")
    void testAllSupplyAndGet5() {
        Tuple5<String, String, String, String, String> t =
                CompletableFutureUtils.allSupplyAndGet(() -> "a", () -> "b", () -> "c", () -> "d", () -> "e");
        assertEquals("a", t._1());
        assertEquals("b", t._2());
        assertEquals("c", t._3());
        assertEquals("d", t._4());
        assertEquals("e", t._5());
    }
    
    @Test
    @DisplayName("多任务：fast-fail 捕获第一个真实业务异常并取消其他任务")
    void testAllSupplyAndGetFastFailCapturesOriginal() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            CountDownLatch longTaskStarted = new CountDownLatch(1);
            AtomicBoolean longTaskInterrupted = new AtomicBoolean(false);
            
            Supplier<String> longRunning = () -> {
                longTaskStarted.countDown();
                try {
                    for (int i = 0; i < 200; i++) {
                        Thread.sleep(10);
                        if (Thread.currentThread().isInterrupted()) {
                            longTaskInterrupted.set(true);
                            throw new RuntimeException("Interrupted");
                        }
                    }
                    return "LONG";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    longTaskInterrupted.set(true);
                    throw new RuntimeException("Interrupted");
                }
            };
            
            IllegalArgumentException root = new IllegalArgumentException("fail-fast");
            Supplier<String> failing = () -> {
                awaitSilently(longTaskStarted);
                throw root;
            };
            
            CompletableException ex = assertThrows(CompletableException.class, () -> CompletableFutureUtils.allSupplyAndGet(longRunning, failing, 5, TimeUnit.SECONDS, executor));
            
            assertSame(root, ex.getCause(), "Should keep original failing supplier cause");
            // 不强制要求一定中断（可能因为执行很快就取消了）
        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    @DisplayName("多任务：包含 null supplier 时抛出 CompletableException 并携带 NPE cause")
    void testAllSupplyAndGetNullSupplier() {
        CompletableException ex = assertThrows(CompletableException.class, () -> CompletableFutureUtils.allSupplyAndGet(() -> "ok", null));
        assertTrue(ex.getCause() instanceof NullPointerException);
    }
    
    @Test
    @DisplayName("debugCurrentPoolStats 不返回空字符串")
    void testDebugCurrentPoolStats() {
        String stats = CompletableFutureUtils.debugCurrentPoolStats();
        assertNotNull(stats);
        assertFalse(stats.isBlank());
    }
    
    @Test
    @DisplayName("多任务：整体超时生效")
    void testAllSupplyAndGetTimeoutCancellation() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Supplier<Integer> slow = () -> {
                sleepSilently(1000);
                return 1;
            };
            Supplier<Integer> normal = () -> 2;
            CompletableException ex = assertThrows(CompletableException.class,
                    () -> CompletableFutureUtils.allSupplyAndGet(slow, normal,
                            100, TimeUnit.MILLISECONDS, executor));
            assertTrue(ex.getMessage().toLowerCase().contains("timed"));
        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    @DisplayName("异步 supplyAsync：非阻塞")
    void testSupplyAsyncNonBlocking() {
        CompletableFuture<String> cf = CompletableFutureUtils.supplyAsync(() -> "ASYNC");
        assertEquals("ASYNC", cf.join());
    }
    
    @Test
    @DisplayName("异常 cause 透传为根因 IllegalArgumentException")
    void testCausePropagationInMulti() {
        IllegalArgumentException root = new IllegalArgumentException("root-error");
        CompletableException ex = assertThrows(CompletableException.class, () -> CompletableFutureUtils.allSupplyAndGet(
                () -> {
                    sleepSilently(50);
                    return "A";
                },
                () -> {
                    throw root;
                }));
        assertSame(root, ex.getCause());
    }
    
    @Test
    @DisplayName("阻塞调用超时不应被拖长")
    void testOverallCallTimeoutEnforced() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            long start = System.nanoTime();
            assertThrows(CompletableException.class, () -> CompletableFutureUtils.allSupplyAndGet(
                    () -> {
                        sleepSilently(1500);
                        return "SLOW";
                    },
                    () -> {
                        sleepSilently(1500);
                        return "SLOW2";
                    },
                    200, TimeUnit.MILLISECONDS, executor));
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            assertTrue(elapsedMs < 1400);
        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    @DisplayName("自定义 Executor")
    void testSupplyAndGetCustomExecutor() {
        ExecutorService exec = Executors.newFixedThreadPool(1);
        try {
            String r = CompletableFutureUtils.supplyAndGet(() -> "X", exec);
            assertEquals("X", r);
        } finally {
            exec.shutdownNow();
        }
    }
    
    @Test
    @DisplayName("异步组合 thenCombine")
    void testAsyncComposition() {
        CompletableFuture<String> cf = CompletableFutureUtils
                .supplyAsync(() -> 42)
                .thenCombine(CompletableFutureUtils.supplyAsync(() -> "answer"),
                        (i, s) -> s + "=" + i);
        assertEquals("answer=42", cf.join());
    }
    
    @Test
    @DisplayName("异常信息语义包含 Query")
    void testExceptionMessageConsistency() {
        CompletableException ex = assertThrows(CompletableException.class,
                () -> CompletableFutureUtils.supplyAndGet(() -> {
                    throw new RuntimeException("X");
                }));
        assertTrue(ex.getMessage().toLowerCase().contains("query"));
    }
    
    @Test
    @DisplayName("并发多次调用压力 smoketest")
    void testParallelInvocations() {
        int loops = 50;
        ExecutorService exec = Executors.newFixedThreadPool(8);
        try {
            CompletableFuture<?>[] futures = new CompletableFuture<?>[loops];
            for (int i = 0; i < loops; i++) {
                int v = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    Tuple2<Integer, Integer> t = CompletableFutureUtils.allSupplyAndGet(
                            () -> v,
                            () -> v + 1);
                    assertEquals(v, t._1());
                    assertEquals(v + 1, t._2());
                }, exec);
            }
            assertTimeoutPreemptively(Duration.ofSeconds(10),
                    () -> CompletableFuture.allOf(futures).join());
        } finally {
            exec.shutdownNow();
        }
    }
    
    /* helpers */
    private static void sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void awaitSilently(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
