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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : wh
 * @date : 2025/6/19
 * @description:
 */
@Slf4j
public class CompletableFutureUtils {
    
    // 这样可以更好地控制线程的生命周期、队列和拒绝策略
    private static final ExecutorService DEFAULT_EXECUTOR;
    
    static {
        DEFAULT_EXECUTOR = ThreadPoolBuilder
                .ioThreadPoolBuilder()
                .setThreadNamePrefix("database-query-")
                .setMaximumPoolSize(20)
                .builder(10, 20);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DEFAULT_EXECUTOR.shutdownNow();
            try {
                if (!DEFAULT_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Executor did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
    
    // 默认超时时间
    private static final long DEFAULT_TIMEOUT = 8;
    // 默认时间单位
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    
    /**
     * Executes a single asynchronous task and retrieves the result with a default timeout.
     *
     * @param supplier The supplier for the asynchronous task.
     * @param <T>      The type of the result.
     * @return The result of the asynchronous task.
     * @throws CompletableException If the query times out or another exception occurs.
     */
    public static <T> T supplyAndGet(Supplier<T> supplier) throws CompletableException {
        return supplyAndGet(supplier, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, DEFAULT_EXECUTOR);
    }
    
    public static <T> T supplyAndGet(Supplier<T> supplier, Executor executor) {
        return supplyAndGet(supplier, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, executor);
    }

    public static <T> T supplyAndGet(Supplier<T> supplier, long timeout, TimeUnit unit) {
        return supplyAndGet(supplier, timeout, unit, DEFAULT_EXECUTOR);
    }
    
    /**
     * Executes a single asynchronous task and retrieves the result with a specified timeout.
     *
     * @param supplier The supplier for the asynchronous task.
     * @param timeout  The timeout duration.
     * @param unit     The time unit for the timeout.
     * @param <T>      The type of the result.
     * @return The result of the asynchronous task.
     * @throws CompletableException If the query times out or another exception occurs.
     */
    public static <T> T supplyAndGet(Supplier<T> supplier, long timeout, TimeUnit unit,
                                     Executor executor) throws CompletableException {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, executor);
        return getFutureResult(future, timeout, unit);
    }
    
    /**
     * 批量执行两个异步任务并获取结果，以 Tuple2 形式返回，带默认超时时间
     */
    public static <R1, R2> Tuple2<R1, R2> allSupplyAndGet(Supplier<R1> supplier1,
                                                          Supplier<R2> supplier2) throws CompletableException {
        return allSupplyAndGet(supplier1, supplier2, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }
    
    /**
     * 批量执行两个异步任务并获取结果，以 Tuple2 形式返回，可指定超时时间
     */
    public static <R1, R2> Tuple2<R1, R2> allSupplyAndGet(Supplier<R1> supplier1, Supplier<R2> supplier2, long timeout,
                                                          TimeUnit unit, Executor executor) throws CompletableException {
        CompletableFuture<R1> future1 = CompletableFuture.supplyAsync(supplier1, executor);
        CompletableFuture<R2> future2 = CompletableFuture.supplyAsync(supplier2, executor);
        
        return getFutureResult(future1.thenCombine(future2, Tuple::of), timeout, unit);
    }
    
    /**
     * 批量执行两个异步任务并获取结果，以 Tuple2 形式返回，可指定超时时间
     */
    public static <R1, R2> Tuple2<R1, R2> allSupplyAndGet(Supplier<R1> supplier1, Supplier<R2> supplier2, long timeout,
                                                          TimeUnit unit) throws CompletableException {
        // 统一获取结果的方式
        return allSupplyAndGet(supplier1, supplier2, timeout, unit, DEFAULT_EXECUTOR);
    }
    
    /**
     * 批量执行三个异步任务并获取结果，以 Tuple3 形式返回，带默认超时时间
     */
    public static <R1, R2, R3> Tuple3<R1, R2, R3> allSupplyAndGet(Supplier<R1> supplier1, Supplier<R2> supplier2,
                                                                  Supplier<R3> supplier3) throws CompletableException {
        return allSupplyAndGet(supplier1, supplier2, supplier3, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, DEFAULT_EXECUTOR);
    }
    
    /**
     * 批量执行三个异步任务并获取结果，以 Tuple3 形式返回，可指定超时时间
     */
    public static <R1, R2, R3> Tuple3<R1, R2, R3> allSupplyAndGet(Supplier<R1> supplier1, Supplier<R2> supplier2,
                                                                  Supplier<R3> supplier3, long timeout, TimeUnit unit, Executor executor) throws CompletableException {
        CompletableFuture<R1> future1 = CompletableFuture.supplyAsync(supplier1, executor);
        CompletableFuture<R2> future2 = CompletableFuture.supplyAsync(supplier2, executor);
        CompletableFuture<R3> future3 = CompletableFuture.supplyAsync(supplier3, executor);
        
        // 统一获取结果的方式
        return getFutureResult(
                future1.thenCombine(future2, Tuple::of)
                        .thenCombine(future3, (t2, r3) -> Tuple.of(t2._1(), t2._2(), r3)),
                timeout, unit);
    }
    
    /**
     * 批量执行四个异步任务并获取结果，以 Tuple4 形式返回，带默认超时时间
     */
    public static <R1, R2, R3, R4> Tuple4<R1, R2, R3, R4> allSupplyAndGet(Supplier<R1> supplier1,
                                                                          Supplier<R2> supplier2,
                                                                          Supplier<R3> supplier3, Supplier<R4> supplier4) throws CompletableException {
        return allSupplyAndGet(supplier1, supplier2, supplier3, supplier4, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, DEFAULT_EXECUTOR);
    }

    public static <R1, R2, R3, R4, R5> Tuple5<R1, R2, R3, R4, R5> allSupplyAndGet(Supplier<R1> supplier1,
        Supplier<R2> supplier2,
        Supplier<R3> supplier3,
        Supplier<R4> supplier4,
        Supplier<R5> supplier5) throws CompletableException {
        return allSupplyAndGet(supplier1, supplier2, supplier3, supplier4, supplier5, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT, DEFAULT_EXECUTOR);
    }
    
    /**
     * 批量执行四个异步任务并获取结果，以 Tuple4 形式返回，可指定超时时间
     */
    public static <R1, R2, R3, R4> Tuple4<R1, R2, R3, R4> allSupplyAndGet(Supplier<R1> supplier1,
                                                                          Supplier<R2> supplier2,
                                                                          Supplier<R3> supplier3, Supplier<R4> supplier4, long timeout, TimeUnit unit,
                                                                          Executor executor) throws CompletableException {
        CompletableFuture<R1> future1 = CompletableFuture.supplyAsync(supplier1, executor);
        CompletableFuture<R2> future2 = CompletableFuture.supplyAsync(supplier2, executor);
        CompletableFuture<R3> future3 = CompletableFuture.supplyAsync(supplier3, executor);
        CompletableFuture<R4> future4 = CompletableFuture.supplyAsync(supplier4, executor);
        
        return getFutureResult(
                future1.thenCombine(future2, Tuple::of)
                        .thenCombine(future3, (t2, r3) -> Tuple.of(t2._1(), t2._2(), r3))
                        .thenCombine(future4, (t3, r4) -> Tuple.of(t3._1(), t3._2(), t3._3(), r4)),
                timeout, unit);
    }

    public static <R1, R2, R3, R4, R5> Tuple5<R1, R2, R3, R4, R5> allSupplyAndGet(Supplier<R1> supplier1,
        Supplier<R2> supplier2,
        Supplier<R3> supplier3,
        Supplier<R4> supplier4,
        Supplier<R5> supplier5,
        long timeout, TimeUnit unit,
        Executor executor) throws CompletableException {
        CompletableFuture<R1> future1 = CompletableFuture.supplyAsync(supplier1, executor);
        CompletableFuture<R2> future2 = CompletableFuture.supplyAsync(supplier2, executor);
        CompletableFuture<R3> future3 = CompletableFuture.supplyAsync(supplier3, executor);
        CompletableFuture<R4> future4 = CompletableFuture.supplyAsync(supplier4, executor);
        CompletableFuture<R5> future5 = CompletableFuture.supplyAsync(supplier5, executor);

        return getFutureResult(
            future1.thenCombine(future2, Tuple::of)
                .thenCombine(future3, (t2, r3) -> Tuple.of(t2._1(), t2._2(), r3))
                .thenCombine(future4, (t3, r4) -> Tuple.of(t3._1(), t3._2(), t3._3(), r4))
                .thenCombine(future5, (t4, r5) -> Tuple.of(t4._1(), t4._2(), t4._3(), t4._4(), r5)),
            timeout, unit);
    }
    
    /**
     * 内部通用方法，用于处理所有 CompletableFuture 的 get 操作及异常封装。
     * 减少重复的 try-catch 块。
     *
     * @param future  CompletableFuture实例
     * @param timeout 超时时间
     * @param unit    时间单位
     * @param <T>     结果类型
     * @return 异步任务的结果
     * @throws CompletableException 如果查询超时或发生其他异常
     */
    private static <T> T getFutureResult(CompletableFuture<T> future, long timeout,
                                         TimeUnit unit) throws CompletableException {
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            log.error("CompletableFuture query timed out.", e);
            throw new CompletableException("Query timed out, please try again later.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("CompletableFuture query was interrupted.", e);
            throw new CompletableException("Query interrupted, please try again later.");
        } catch (ExecutionException e) {
            log.error("CompletableFuture query execution failed.", e.getCause());
            throw new CompletableException("Query failed, please try again later.");
        }
    }
    
}
