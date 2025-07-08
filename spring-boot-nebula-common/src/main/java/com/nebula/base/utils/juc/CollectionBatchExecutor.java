package com.nebula.base.utils.juc;

import com.google.common.collect.Lists;
import com.nebula.base.utils.DataUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : wh
 * @date : 2025/6/27
 * @description:
 */
@Slf4j
public class CollectionBatchExecutor {

    private CollectionBatchExecutor() {
    }

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

    /**
     * 执行批量任务，使用默认线程池。
     *
     * @param sourceData 源数据列表
     * @param batchSize  批量大小
     * @param batchTask  批量任务函数，接受一个 List<T> 并返回一个 List<R>
     * @param <T>
     * @param <R>
     * @return
     */
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

    /**
     * 差异比对的结果容器。
     * 使用静态内部类避免了对外部Tuple库的依赖。
     *
     * @param <T> 元素的类型
     */
    @Getter
    public static class DiffResult<T> {
        private final List<T> toAdd;
        private final List<T> toUpdate;
        private final List<T> toDelete;

        public DiffResult(List<T> toAdd, List<T> toUpdate, List<T> toDelete) {
            this.toAdd = toAdd;
            this.toUpdate = toUpdate;
            this.toDelete = toDelete;
        }

        public boolean hasChanges() {
            return !(toAdd.isEmpty() && toUpdate.isEmpty() && toDelete.isEmpty());
        }
    }

    /**
     * 比对两个集合，返回一个包含新增、更新和删除列表的结果对象。
     * 这是一个纯函数，没有副作用。
     *
     * @param <T>           集合中元素的数据类型
     * @param <K>           元素的唯一标识符类型
     * @param oldCollection 旧数据集合
     * @param newCollection 新数据集合
     * @param keyExtractor  从元素T中提取其唯一标识符K的函数
     * @return DiffResult<T> 包含差异列表的结果对象
     */
    public static <T, K> DiffResult<T> diff(
        Collection<T> oldCollection,
        Collection<T> newCollection,
        Function<T, K> keyExtractor
    ) {
        Collection<T> oldList = oldCollection == null ? Collections.emptyList() : oldCollection;
        Collection<T> newList = newCollection == null ? Collections.emptyList() : newCollection;

        Map<K, T> oldMap = oldList.stream()
            .collect(Collectors.toMap(keyExtractor, Function.identity(), (existing, replacement) -> existing)); 

        List<T> toAdd = new ArrayList<>();
        List<T> toUpdate = new ArrayList<>();

        for (T newItem : newList) {
            K key = keyExtractor.apply(newItem);
            if (key == null || !oldMap.containsKey(key)) {
                toAdd.add(newItem);
            } else {
                T oldItem = oldMap.get(key);
                if (!newItem.equals(oldItem)) {
                    toUpdate.add(newItem);
                }
                oldMap.remove(key);
            }
        }

        // oldMap中剩余的值就是需要删除的完整对象
        List<T> toDelete = new ArrayList<>(oldMap.values());

        return new DiffResult<>(toAdd, toUpdate, toDelete);
    }

    /**
     * 根据比对结果，执行相应的增删改操作。
     * 此版本接受需要删除的【完整对象列表】。
     *
     * @param oldCollection  旧数据集合
     * @param newCollection  新数据集合
     * @param keyExtractor   提取Key的函数
     * @param addConsumer    处理新增列表的消费者
     * @param updateConsumer 处理更新列表的消费者
     * @param deleteConsumer 处理删除列表的消费者 (接收 List<T>)
     */
    public static <T, K> void batchCRUD(
        Collection<T> oldCollection,
        Collection<T> newCollection,
        Function<T, K> keyExtractor,
        Consumer<List<T>> addConsumer,
        Consumer<List<T>> updateConsumer,
        Consumer<List<T>> deleteConsumer
    ) {
        // 1. 调用diff方法获取差异
        DiffResult<T> result = diff(oldCollection, newCollection, keyExtractor);

        if (DataUtils.isAllNotEmpty(result.getToAdd(), addConsumer)) {
            addConsumer.accept(result.getToAdd());
        }

        if (DataUtils.isAllNotEmpty(result.getToUpdate(), updateConsumer)) {
            updateConsumer.accept(result.getToUpdate());
        }

        if (DataUtils.isAllNotEmpty(result.getToDelete(), deleteConsumer)) {
            deleteConsumer.accept(result.getToDelete());
        }
    }

    /**
     * 
     * 此版本接受需要删除的【Key列表】，为常见用例提供便利。
     *
     * @param oldCollection       旧数据集合
     * @param newCollection       新数据集合
     * @param keyExtractor        提取Key的函数
     * @param addConsumer         处理新增列表的消费者
     * @param updateConsumer      处理更新列表的消费者
     * @param deleteConsumerByKey 处理删除列表的消费者 (接收 List<K>)
     */
    public static <T, K> void batchCRUDByKey(
        Collection<T> oldCollection,
        Collection<T> newCollection,
        Function<T, K> keyExtractor,
        Consumer<List<T>> addConsumer,
        Consumer<List<T>> updateConsumer,
        Consumer<List<K>> deleteConsumerByKey
    ) {
        DiffResult<T> result = diff(oldCollection, newCollection, keyExtractor);

        if (DataUtils.isAllNotEmpty(result.getToAdd(), addConsumer)) {
            addConsumer.accept(result.getToAdd());
        }

        if (DataUtils.isAllNotEmpty(result.getToUpdate(), updateConsumer)) {
            updateConsumer.accept(result.getToUpdate());
        }

        if (!result.getToDelete().isEmpty() && deleteConsumerByKey != null) {
            List<K> toDeleteKeys = result.getToDelete().stream()
                .map(keyExtractor)
                .collect(Collectors.toList());
            deleteConsumerByKey.accept(toDeleteKeys);
        }
    }

}
