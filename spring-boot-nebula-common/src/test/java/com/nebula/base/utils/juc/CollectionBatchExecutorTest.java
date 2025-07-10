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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author : wh
 * @date : 2025/6/27
 * @description:
 */

class CollectionBatchExecutorTest {
    
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
    @DisplayName("[Happy Path] 使用外部线程池成功执行")
    void testExecuteWithExternalExecutor_Success() {
        List<Integer> data = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<String> results = CollectionBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(100, results.size());
        assertTrue(results.contains("Processed:0"));
        assertTrue(results.contains("Processed:99"));
    }
    
    @Test
    @DisplayName("[Happy Path] 使用内部默认线程池成功执行")
    void testExecuteWithDefaultExecutor_Success() {
        List<Integer> data = IntStream.range(0, 50).boxed().collect(Collectors.toList());
        List<String> results = CollectionBatchExecutor.execute(data, 5, slowTask);
        assertEquals(50, results.size());
        assertTrue(results.contains("Processed:49"));
    }
    
    @Test
    @DisplayName("[Performance] 并行执行应该比串行快")
    void testParallelExecutionIsFaster() {
        List<Integer> data = IntStream.range(0, 100).boxed().collect(Collectors.toList()); // 100个任务，每个50ms
        
        // 串行执行预计耗时: 100/10 * 50ms = 10 * 50ms = 500ms
        long sequentialTime = 500;
        
        // 并行执行（10个线程）预计耗时：约等于一个批次的时间 50ms (+开销)
        assertTimeout(Duration.ofMillis(sequentialTime), () -> {
            CollectionBatchExecutor.execute(data, 10, slowTask, executorService);
        }, "并行执行超时，可能没有真正并行！");
    }
    
    @Test
    @DisplayName("[Edge Case] 输入为空集合")
    void testExecute_EmptySource() {
        List<String> results = CollectionBatchExecutor.execute(Collections.emptyList(), 10, slowTask, executorService);
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("[Edge Case] 输入为null")
    void testExecute_NullSource() {
        List<String> results = CollectionBatchExecutor.execute(null, 10, slowTask, executorService);
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("엣 [Edge Case] 批次大小大于总数")
    void testExecute_BatchSizeLargerThanTotal() {
        List<Integer> data = IntStream.range(0, 5).boxed().collect(Collectors.toList());
        List<String> results = CollectionBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(5, results.size());
    }
    
    @Test
    @DisplayName("[Edge Case] 数据量是批次的整数倍")
    void testExecute_SizeIsMultipleOfBatchSize() {
        List<Integer> data = IntStream.range(0, 30).boxed().collect(Collectors.toList());
        List<String> results = CollectionBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(30, results.size());
    }
    
    @Test
    @DisplayName("[Edge Case] 数据量不是批次的整数倍")
    void testExecute_SizeIsNotMultipleOfBatchSize() {
        List<Integer> data = IntStream.range(0, 33).boxed().collect(Collectors.toList());
        List<String> results = CollectionBatchExecutor.execute(data, 10, slowTask, executorService);
        assertEquals(33, results.size());
        assertTrue(results.contains("Processed:32"));
    }
    
    @Test
    @DisplayName("[Validation] 批次大小为0时抛出异常")
    void testExecute_InvalidBatchSize() {
        List<Integer> data = List.of(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> CollectionBatchExecutor.execute(data, 0, slowTask, executorService));
    }
    
    @Test
    @DisplayName("[Validation] 任务函数为null时抛出异常")
    void testExecute_NullTask() {
        List<Integer> data = List.of(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> CollectionBatchExecutor.execute(data, 1, null, executorService));
    }
    
    @Getter
    private static class TestItem {
        
        private Long id;
        
        private String data;
        
        public TestItem(Long id, String data) {
            this.id = id;
            this.data = data;
        }
        
        // 重写 equals 和 hashCode 以确保基于内容的比较
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TestItem testItem = (TestItem) o;
            return Objects.equals(id, testItem.id) && Objects.equals(data, testItem.data);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id, data);
        }
    }
    
    @Test
    @DisplayName("应该找出所有需要新增的项")
    void shouldIdentifyItemsToAdd() {
        List<TestItem> oldList = List.of();
        List<TestItem> newList = List.of(new TestItem(1L, "A"), new TestItem(2L, "B"));
        
        CollectionBatchExecutor.DiffResult<TestItem> result = CollectionBatchExecutor.diff(oldList, newList, TestItem::getId);
        
        assertEquals(2, result.getToAdd().size());
        assertTrue(result.getToAdd().containsAll(newList));
        assertTrue(result.getToUpdate().isEmpty());
        assertTrue(result.getToDelete().isEmpty());
    }
    
    @Test
    @DisplayName("应该找出所有需要删除的项")
    void shouldIdentifyItemsToDelete() {
        List<TestItem> oldList = List.of(new TestItem(1L, "A"), new TestItem(2L, "B"));
        List<TestItem> newList = List.of();
        
        CollectionBatchExecutor.DiffResult<TestItem> result = CollectionBatchExecutor.diff(oldList, newList, TestItem::getId);
        
        assertTrue(result.getToAdd().isEmpty());
        assertTrue(result.getToUpdate().isEmpty());
        assertEquals(2, result.getToDelete().size());
        assertTrue(result.getToDelete().containsAll(oldList));
    }
    
    @Test
    @DisplayName("应该找出所有需要更新的项")
    void shouldIdentifyItemsToUpdate() {
        List<TestItem> oldList = List.of(new TestItem(1L, "A"), new TestItem(2L, "B"));
        List<TestItem> newList = List.of(new TestItem(1L, "A-updated"), new TestItem(2L, "B-updated"));
        
        CollectionBatchExecutor.DiffResult<TestItem> result = CollectionBatchExecutor.diff(oldList, newList, TestItem::getId);
        
        assertTrue(result.getToAdd().isEmpty());
        assertEquals(2, result.getToUpdate().size());
        assertTrue(result.getToUpdate().containsAll(newList));
        assertTrue(result.getToDelete().isEmpty());
    }
    
    @Test
    @DisplayName("应该能处理混合场景（增、删、改）")
    void shouldHandleMixedScenario() {
        // Arrange: 准备数据
        TestItem itemToUpdate = new TestItem(1L, "A");
        TestItem itemToDelete = new TestItem(2L, "B");
        List<TestItem> oldList = List.of(itemToUpdate, itemToDelete);
        
        TestItem itemUpdated = new TestItem(1L, "A-updated");
        TestItem itemToAdd = new TestItem(3L, "C");
        List<TestItem> newList = List.of(itemUpdated, itemToAdd);
        
        // Act: 执行diff
        CollectionBatchExecutor.DiffResult<TestItem> result = CollectionBatchExecutor.diff(oldList, newList, TestItem::getId);
        
        // Assert: 验证结果
        assertEquals(1, result.getToAdd().size());
        assertEquals(itemToAdd, result.getToAdd().get(0));
        
        assertEquals(1, result.getToUpdate().size());
        assertEquals(itemUpdated, result.getToUpdate().get(0));
        
        assertEquals(1, result.getToDelete().size());
        assertEquals(itemToDelete, result.getToDelete().get(0));
    }
    
    @Test
    @DisplayName("当集合没有变化时，不应返回任何差异")
    void shouldReturnNoChangesWhenListsAreIdentical() {
        List<TestItem> oldList = List.of(new TestItem(1L, "A"));
        List<TestItem> newList = List.of(new TestItem(1L, "A"));
        
        CollectionBatchExecutor.DiffResult<TestItem> result = CollectionBatchExecutor.diff(oldList, newList, TestItem::getId);
        
        assertFalse(result.hasChanges());
        assertTrue(result.getToAdd().isEmpty());
        assertTrue(result.getToUpdate().isEmpty());
        assertTrue(result.getToDelete().isEmpty());
    }
    
    @Test
    @DisplayName("应该能正确处理null集合输入")
    void shouldHandleNullCollections() {
        // new is null
        var result1 = CollectionBatchExecutor.diff(List.of(new TestItem(1L, "A")), null, TestItem::getId);
        assertTrue(result1.getToAdd().isEmpty());
        assertTrue(result1.getToUpdate().isEmpty());
        assertEquals(1, result1.getToDelete().size());
        
        // old is null
        var result2 = CollectionBatchExecutor.diff(null, List.of(new TestItem(1L, "A")), TestItem::getId);
        assertEquals(1, result2.getToAdd().size());
        assertTrue(result2.getToUpdate().isEmpty());
        assertTrue(result2.getToDelete().isEmpty());
        
        // both are null
        var result3 = CollectionBatchExecutor.diff(null, null, TestItem::getId);
        assertFalse(result3.hasChanges());
    }
    
}