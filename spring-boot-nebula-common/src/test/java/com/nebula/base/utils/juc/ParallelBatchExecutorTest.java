package com.nebula.base.utils.juc;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author : wh
 * @date : 2025/6/27
 * @description:
 */
class ParallelBatchExecutorTest {

    private ExecutorService executorService;

    private final Function<List<Integer>, List<String>> slowTask = batch -> {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return batch.stream().map(i -> "Processed:" + i).collect(Collectors.toList());

    };

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

    }

    @Test
    @DisplayName("✅ [Happy Path] 使用外部线程池成功执行")
    void testExecuteWithExternalExecutor_Success() {
        List<Integer> data = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<String> results = ParallelBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(100, results.size());
        assertTrue(results.contains("Processed:0"));
        assertTrue(results.contains("Processed:99"));
    }

    @Test
    @DisplayName("✅ [Happy Path] 使用内部默认线程池成功执行")
    void testExecuteWithDefaultExecutor_Success() {
        List<Integer> data = IntStream.range(0, 50).boxed().collect(Collectors.toList());
        List<String> results = ParallelBatchExecutor.execute(data, 5, slowTask);
        assertEquals(50, results.size());
        assertTrue(results.contains("Processed:49"));
    }

    @Test
    @DisplayName("⏱️ [Performance] 并行执行应该比串行快")
    void testParallelExecutionIsFaster() {
        List<Integer> data = IntStream.range(0, 100).boxed().collect(Collectors.toList()); // 100个任务，每个50ms

        // 串行执行预计耗时: 100/10 * 50ms = 10 * 50ms = 500ms
        long sequentialTime = 500;

        // 并行执行（10个线程）预计耗时：约等于一个批次的时间 50ms (+开销)
        assertTimeout(Duration.ofMillis(sequentialTime), () -> {
            ParallelBatchExecutor.execute(data, 10, slowTask, executorService);
        }, "并行执行超时，可能没有真正并行！");
    }

    @Test
    @DisplayName("[Edge Case] 输入为空集合")
    void testExecute_EmptySource() {
        List<String> results = ParallelBatchExecutor.execute(Collections.emptyList(), 10, slowTask, executorService);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("[Edge Case] 输入为null")
    void testExecute_NullSource() {
        List<String> results = ParallelBatchExecutor.execute(null, 10, slowTask, executorService);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("엣 [Edge Case] 批次大小大于总数")
    void testExecute_BatchSizeLargerThanTotal() {
        List<Integer> data = IntStream.range(0, 5).boxed().collect(Collectors.toList());
        List<String> results = ParallelBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(5, results.size());
    }

    @Test
    @DisplayName("엣 [Edge Case] 数据量是批次的整数倍")
    void testExecute_SizeIsMultipleOfBatchSize() {
        List<Integer> data = IntStream.range(0, 30).boxed().collect(Collectors.toList());
        List<String> results = ParallelBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(30, results.size());
    }

    @Test
    @DisplayName("엣 [Edge Case] 数据量不是批次的整数倍")
    void testExecute_SizeIsNotMultipleOfBatchSize() {
        List<Integer> data = IntStream.range(0, 33).boxed().collect(Collectors.toList());
        List<String> results = ParallelBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(33, results.size());
        assertTrue(results.contains("Processed:32"));
    }

    @Test
    @DisplayName("🛑 [Validation] 批次大小为0时抛出异常")
    void testExecute_InvalidBatchSize() {
        List<Integer> data = List.of(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () ->
            ParallelBatchExecutor.execute(data, 0, slowTask, executorService)
        );
    }

    @Test
    @DisplayName("🛑 [Validation] 任务函数为null时抛出异常")
    void testExecute_NullTask() {
        List<Integer> data = List.of(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () ->
            ParallelBatchExecutor.execute(data, 1, null, executorService)
        );
    }
    


}