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
 
package com.nebula.alert.feishu;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.nebula.web.common.utils.NebulaSysWebUtils;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FeiShuRootTest {
    
    @Test
    void destroyShutsDownExecutor() throws Exception {
        FeiShuRoot root = new FeiShuRoot(mock(NebulaSysWebUtils.class));
        
        root.destroy();
        
        // destroy 后再提交应触发拒绝策略（CallerRunsPolicy 在此场景由测试线程执行任务，
        // 这里验证线程池已进入终止态）
        assertTrue(root.getThreadPoolTaskExecutor().isShutdown());
    }
    
    @Test
    void asyncSendExceptionDoesNotPropagate() {
        FeiShuRoot root = new FeiShuRoot(mock(NebulaSysWebUtils.class));
        CountDownLatch latch = new CountDownLatch(1);
        root.getThreadPoolTaskExecutor().submit(() -> {
            try {
                throw new RejectedExecutionException("simulated");
            } finally {
                latch.countDown();
            }
        });
        assertDoesNotThrow(() -> {
            root.sendRichTextAsync(null, "fmt %s", "arg");
            latch.await(2, TimeUnit.SECONDS);
        });
    }
}
