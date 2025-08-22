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
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : wh
 * @date : 2025/6/19
 * @description:
 */
@Slf4j
public class CompletableFutureUtils {
    
    private static final long DEFAULT_TIMEOUT = 8;
    
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    
    private static final boolean DEFAULT_FAST_FAIL = true;
    
    private CompletableFutureUtils() {
    }
    
    private static class DefaultExecutorHolder {
        
        private static final ExecutorService EXECUTOR = buildDefaultExecutor();
        private static ExecutorService buildDefaultExecutor() {
            ExecutorService pool = ThreadPoolBuilder
                    .ioBoundBuilder()
                    .setThreadNamePrefix("database-query-")
                    .setMaximumPoolSize(20)
                    .setQueueSize(10000)
                    .build(100, 20);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(pool, 5, TimeUnit.SECONDS)));
            return pool;
        }
    }
    
    private static ExecutorService defaultExecutor() {
        return DefaultExecutorHolder.EXECUTOR;
    }
    
    public static void shutdownDefaultExecutor() {
        shutdown(defaultExecutor(), 10, TimeUnit.SECONDS);
    }
    
    private static void shutdown(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                log.warn("Executor did not terminate within {} {}", timeout, unit);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while awaiting executor termination", e);
        }
    }
    
    /* 单任务 */
    public static <T> T supplyAndGet(Supplier<T> supplier) throws CompletableException {
        return supplyAndGet(supplier, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, defaultExecutor());
    }
    
    public static <T> T supplyAndGet(Supplier<T> supplier, Executor executor) {
        return supplyAndGet(supplier, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, executor);
    }
    
    public static <T> T supplyAndGet(Supplier<T> supplier, long timeout, TimeUnit unit) {
        return supplyAndGet(supplier, timeout, unit, defaultExecutor());
    }
    
    public static <T> T supplyAndGet(Supplier<T> supplier,
                                     long timeout,
                                     TimeUnit unit,
                                     Executor executor) throws CompletableException {
        Objects.requireNonNull(supplier, "supplier");
        CompletableFuture<T> future = startAsync(supplier, executor);
        return blockingGet(future, timeout, unit);
    }
    
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return startAsync(supplier, defaultExecutor());
    }
    
    /* 多任务 2~5 */
    public static <R1, R2> Tuple2<R1, R2> allSupplyAndGet(Supplier<R1> s1,
                                                          Supplier<R2> s2) throws CompletableException {
        return allSupplyAndGet(s1, s2, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, defaultExecutor());
    }
    
    public static <R1, R2> Tuple2<R1, R2> allSupplyAndGet(Supplier<R1> s1,
                                                          Supplier<R2> s2,
                                                          long timeout,
                                                          TimeUnit unit) throws CompletableException {
        return allSupplyAndGet(s1, s2, timeout, unit, defaultExecutor());
    }
    
    public static <R1, R2> Tuple2<R1, R2> allSupplyAndGet(Supplier<R1> s1,
                                                          Supplier<R2> s2,
                                                          long timeout,
                                                          TimeUnit unit,
                                                          Executor executor) throws CompletableException {
        Object[] arr = runAll(timeout, unit, executor, DEFAULT_FAST_FAIL, s1, s2);
        @SuppressWarnings("unchecked")
        R1 r1 = (R1) arr[0];
        @SuppressWarnings("unchecked")
        R2 r2 = (R2) arr[1];
        return Tuple.of(r1, r2);
    }
    
    public static <R1, R2, R3> Tuple3<R1, R2, R3> allSupplyAndGet(Supplier<R1> s1,
                                                                  Supplier<R2> s2,
                                                                  Supplier<R3> s3) throws CompletableException {
        return allSupplyAndGet(s1, s2, s3, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, defaultExecutor());
    }
    
    public static <R1, R2, R3> Tuple3<R1, R2, R3> allSupplyAndGet(Supplier<R1> s1,
                                                                  Supplier<R2> s2,
                                                                  Supplier<R3> s3,
                                                                  long timeout,
                                                                  TimeUnit unit,
                                                                  Executor executor) throws CompletableException {
        Object[] arr = runAll(timeout, unit, executor, DEFAULT_FAST_FAIL, s1, s2, s3);
        @SuppressWarnings("unchecked")
        R1 r1 = (R1) arr[0];
        @SuppressWarnings("unchecked")
        R2 r2 = (R2) arr[1];
        @SuppressWarnings("unchecked")
        R3 r3 = (R3) arr[2];
        return Tuple.of(r1, r2, r3);
    }
    
    public static <R1, R2, R3, R4> Tuple4<R1, R2, R3, R4> allSupplyAndGet(Supplier<R1> s1,
                                                                          Supplier<R2> s2,
                                                                          Supplier<R3> s3,
                                                                          Supplier<R4> s4) throws CompletableException {
        return allSupplyAndGet(s1, s2, s3, s4, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, defaultExecutor());
    }
    
    public static <R1, R2, R3, R4> Tuple4<R1, R2, R3, R4> allSupplyAndGet(Supplier<R1> s1,
                                                                          Supplier<R2> s2,
                                                                          Supplier<R3> s3,
                                                                          Supplier<R4> s4,
                                                                          long timeout,
                                                                          TimeUnit unit,
                                                                          Executor executor) throws CompletableException {
        Object[] arr = runAll(timeout, unit, executor, DEFAULT_FAST_FAIL, s1, s2, s3, s4);
        @SuppressWarnings("unchecked")
        R1 r1 = (R1) arr[0];
        @SuppressWarnings("unchecked")
        R2 r2 = (R2) arr[1];
        @SuppressWarnings("unchecked")
        R3 r3 = (R3) arr[2];
        @SuppressWarnings("unchecked")
        R4 r4 = (R4) arr[3];
        return Tuple.of(r1, r2, r3, r4);
    }
    
    public static <R1, R2, R3, R4, R5> Tuple5<R1, R2, R3, R4, R5> allSupplyAndGet(Supplier<R1> s1,
                                                                                  Supplier<R2> s2,
                                                                                  Supplier<R3> s3,
                                                                                  Supplier<R4> s4,
                                                                                  Supplier<R5> s5) throws CompletableException {
        return allSupplyAndGet(s1, s2, s3, s4, s5, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, defaultExecutor());
    }
    
    public static <R1, R2, R3, R4, R5> Tuple5<R1, R2, R3, R4, R5> allSupplyAndGet(Supplier<R1> s1,
                                                                                  Supplier<R2> s2,
                                                                                  Supplier<R3> s3,
                                                                                  Supplier<R4> s4,
                                                                                  Supplier<R5> s5,
                                                                                  long timeout,
                                                                                  TimeUnit unit,
                                                                                  Executor executor) throws CompletableException {
        Object[] arr = runAll(timeout, unit, executor, DEFAULT_FAST_FAIL, s1, s2, s3, s4, s5);
        @SuppressWarnings("unchecked")
        R1 r1 = (R1) arr[0];
        @SuppressWarnings("unchecked")
        R2 r2 = (R2) arr[1];
        @SuppressWarnings("unchecked")
        R3 r3 = (R3) arr[2];
        @SuppressWarnings("unchecked")
        R4 r4 = (R4) arr[3];
        @SuppressWarnings("unchecked")
        R5 r5 = (R5) arr[4];
        return Tuple.of(r1, r2, r3, r4, r5);
    }
    
    /* Internal core */
    private static <T> CompletableFuture<T> startAsync(Supplier<T> supplier, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        }, executor);
    }
    
    @SafeVarargs
    private static Object[] runAll(long timeout,
                                   TimeUnit unit,
                                   Executor executor,
                                   boolean fastFail,
                                   Supplier<?>... suppliers) throws CompletableException {
        try {
            return blockingGet(allSupplyAsyncInternal(executor, fastFail, suppliers), timeout, unit);
        } catch (CompletableException e) {
            throw e;
        }
    }
    
    @SafeVarargs
    private static CompletableFuture<Object[]> allSupplyAsyncInternal(Executor executor,
                                                                      boolean fastFail,
                                                                      Supplier<?>... suppliers) {
        Objects.requireNonNull(executor, "executor");
        if (suppliers == null || suppliers.length == 0) {
            throw new IllegalArgumentException("No suppliers provided");
        }
        
        @SuppressWarnings("unchecked")
        CompletableFuture<Object>[] futures = new CompletableFuture[suppliers.length];
        AtomicBoolean alreadyFailed = new AtomicBoolean(false);
        AtomicReference<Throwable> firstFailure = new AtomicReference<>(null);
        
        for (int i = 0; i < suppliers.length; i++) {
            final int idx = i;
            Supplier<?> supplier = suppliers[i];
            if (supplier == null) {
                futures[i] = failedFuture(new NullPointerException("Supplier at index " + i + " is null"));
                continue;
            }
            futures[i] = startAsync(supplier, executor)
                    .whenComplete((r, ex) -> {
                        if (ex != null) {
                            Throwable cause = unwrapCompletion(ex);
                            // 记录第一个非 Cancellation 的真实异常
                            if (!(cause instanceof CancellationException)) {
                                firstFailure.compareAndSet(null, cause);
                            }
                            if (fastFail && alreadyFailed.compareAndSet(false, true)) {
                                cancelOthers(futures, idx);
                            }
                        }
                    })
                    .thenApply(o -> o);
        }
        
        return CompletableFuture.allOf(futures)
                .handle((ignored, ex) -> {
                    if (ex != null) {
                        Throwable recorded = firstFailure.get();
                        if (recorded != null) {
                            throw new CompletionException(recorded);
                        }
                        // fallback：从 futures 中再找一次（保持兼容）
                        Throwable extracted = extractFirstFailure(futures, ex);
                        throw new CompletionException(extracted);
                    }
                    Object[] results = new Object[futures.length];
                    for (int i = 0; i < futures.length; i++) {
                        results[i] = futures[i].join();
                    }
                    return results;
                });
    }
    
    private static Throwable unwrapCompletion(Throwable t) {
        if (t instanceof CompletionException) {
            CompletionException ce = (CompletionException) t;
            if (Objects.nonNull(ce.getCause())) {
                return ce.getCause();
            }
            
        }
        return t;
    }
    
    private static void cancelOthers(CompletableFuture<?>[] futures, int excludeIdx) {
        for (int i = 0; i < futures.length; i++) {
            if (i != excludeIdx) {
                futures[i].cancel(true);
            }
        }
    }
    
    private static Throwable extractFirstFailure(CompletableFuture<?>[] futures, Throwable aggregate) {
        Throwable firstCancellation = null;
        for (CompletableFuture<?> f : futures) {
            if (f.isCompletedExceptionally() || f.isCancelled()) {
                try {
                    f.join();
                } catch (CompletionException ce) {
                    Throwable cause = ce.getCause();
                    if (cause instanceof CancellationException) {
                        if (firstCancellation == null)
                            firstCancellation = cause;
                    } else {
                        return cause != null ? cause : ce;
                    }
                } catch (CancellationException ce) {
                    if (firstCancellation == null)
                        firstCancellation = ce;
                }
            }
        }
        return firstCancellation != null ? firstCancellation : aggregate;
    }
    
    private static <T> T blockingGet(CompletableFuture<T> future, long timeout, TimeUnit unit) throws CompletableException {
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.error("CompletableFuture timed out ({} {})", timeout, unit);
            throw new CompletableException("Query timed out, please try again later.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("CompletableFuture interrupted", e);
            throw new CompletableException("Query interrupted, please try again later.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("CompletableFuture execution failed: {}", cause.toString(), cause);
            throw new CompletableException("Query failed, please try again later.", cause);
        } catch (CancellationException e) {
            log.error("CompletableFuture was cancelled", e);
            throw new CompletableException("Query cancelled.", e);
        }
    }
    
    private static <T> CompletableFuture<T> failedFuture(Throwable t) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(t);
        return cf;
    }
    
    public static String debugCurrentPoolStats() {
        ExecutorService exec = defaultExecutor();
        if (exec instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) exec;
            return String.format("poolSize=%d, active=%d, queued=%d, completed=%d",
                    tpe.getPoolSize(), tpe.getActiveCount(), tpe.getQueue().size(), tpe.getCompletedTaskCount());
        }
        return exec.toString();
    }
}
