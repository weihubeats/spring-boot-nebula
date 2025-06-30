package com.nebula.base.utils.juc;

import com.google.common.collect.Lists;
import com.nebula.base.utils.DataUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : wh
 * @date : 2025/6/27
 * @description:
 */
@Slf4j
public class ParallelBatchExecutor {

    private static final ExecutorService DEFAULT_EXECUTOR;

    static {
        DEFAULT_EXECUTOR = ThreadPoolBuilder
            .ioBoundBuilder()
            .setThreadNamePrefix("parallel-batch-")
            .setMaximumPoolSize(20)
            .setQueueSize(10000)
            .build(100, 20);

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

    private ParallelBatchExecutor() {
    }

    public static <T, R> List<R> execute(List<T> sourceData, int batchSize,
        Function<List<T>, List<R>> batchTask) {
        if (DataUtils.isEmpty(sourceData)) {
            return Collections.emptyList();
        }
        return execute(sourceData, batchSize, batchTask, DEFAULT_EXECUTOR);

    }

    public static <T, R> List<R> execute(
        List<T> sourceData,
        int batchSize,
        Function<List<T>, List<R>> batchTask,
        ExecutorService executor) {

        if (DataUtils.isEmpty(sourceData)) {
            return Collections.emptyList();
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than 0.");
        }
        if (batchTask == null || executor == null) {
            throw new IllegalArgumentException("Batch task and executor cannot be null.");
        }

        List<List<T>> partitions = partition(new ArrayList<>(sourceData), batchSize);

        List<CompletableFuture<List<R>>> futures = partitions.stream()
            .map(partition -> CompletableFuture.supplyAsync(() -> batchTask.apply(partition), executor))
            .collect(Collectors.toList());

        // 3. 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join();
        } catch (CompletionException e) {
            futures.forEach(f -> f.cancel(true));
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            futures.forEach(f -> f.cancel(true));
            throw new RuntimeException("An unexpected error occurred during parallel execution.", e);
        }
        return futures.stream()
            .map(CompletableFuture::join) // 此处的 join 不会再抛出受检异常，因为上面已经处理过了
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private static <T> List<List<T>> partition(List<T> list, int batchSize) {
        return Lists.partition(list, batchSize);
    }

}
