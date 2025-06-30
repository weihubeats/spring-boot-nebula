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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : wh
 * @date : 2023/11/18 11:27
 * @description:
 */
public class ThreadPoolBuilder {

    /**
     * 获取 CPU 密集型任务的线程池构建器。
     * 这种线程池的核心线程数通常设置为 CPU 核心数，以减少上下文切换。
     *
     * @return CPUThreadPoolBuilder 实例
     */
    public static CPUThreadPoolBuilder cpuBoundBuilder() {
        return new CPUThreadPoolBuilder();
    }

    /**
     * 获取 IO 密集型任务的线程池构建器。
     * 这种线程池的核心线程数会根据预期的 IO 时间和 CPU 计算时间的比率进行调整，
     * 以便在线程等待 IO 时，CPU 资源可以被其他线程利用。
     *
     * @return IOThreadPoolBuilder 实例
     */
    public static IOThreadPoolBuilder ioBoundBuilder() {
        return new IOThreadPoolBuilder();
    }

    /**
     * @param <T>
     */
    public abstract static class AbstractThreadPoolBuilder<T extends AbstractThreadPoolBuilder<T>> {

        protected static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

        protected static final RejectedExecutionHandler DEFAULT_REJECT_HANDLER = new ThreadPoolExecutor.AbortPolicy();

        protected String threadNamePrefix = "pool-thread";

        protected boolean daemon = false;

        protected RejectedExecutionHandler rejectHandler = DEFAULT_REJECT_HANDLER;

        protected int queueSize = -1; // -1 表示使用无界队列

        protected int maximumPoolSize = CPU_COUNT * 2;

        protected long keepAliveTime = 60L;

        protected TimeUnit timeUnit = TimeUnit.SECONDS;

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T setThreadNamePrefix(String threadNamePrefix) {
            if (threadNamePrefix != null && !threadNamePrefix.trim().isEmpty()) {
                this.threadNamePrefix = threadNamePrefix;
            }
            return self();
        }

        public T setDaemon(boolean daemon) {
            this.daemon = daemon;
            return self();
        }

        public T setRejectHandler(RejectedExecutionHandler rejectHandler) {
            if (rejectHandler != null) {
                this.rejectHandler = rejectHandler;
            }
            return self();
        }

        public T setQueueSize(int queueSize) {
            this.queueSize = queueSize;
            return self();
        }

        public T setMaximumPoolSize(int maximumPoolSize) {
            if (maximumPoolSize > 0) {
                this.maximumPoolSize = maximumPoolSize;
            }
            return self();
        }

        public T setKeepAliveTime(long keepAliveTime, TimeUnit timeUnit) {
            if (keepAliveTime > 0) {
                this.keepAliveTime = keepAliveTime;
                this.timeUnit = timeUnit != null ? timeUnit : TimeUnit.SECONDS;
            }
            return self();
        }

        /**
         * 创建并返回配置好的 ThreadPoolExecutor 实例。
         *
         * @param corePoolSize 核心线程数
         * @return ThreadPoolExecutor 实例
         */
        protected ThreadPoolExecutor build(int corePoolSize) {
            // 确保最大池大小不小于核心池大小
            int maxPoolSize = Math.max(corePoolSize, this.maximumPoolSize);

            ThreadFactory threadFactory = new ThreadFactoryImpl(this.threadNamePrefix, this.daemon);

            BlockingQueue<Runnable> queue = (this.queueSize < 1)
                ? new LinkedBlockingQueue<>()
                : new ArrayBlockingQueue<>(this.queueSize);

            return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                this.keepAliveTime,
                this.timeUnit,
                queue,
                threadFactory,
                this.rejectHandler
            );
        }
    }

    /**
     * CPU 密集型线程池构建器。
     */
    public static class CPUThreadPoolBuilder extends AbstractThreadPoolBuilder<CPUThreadPoolBuilder> {

        /**
         * 获取为 CPU 密集型任务优化的核心线程数。
         * 通常等于 CPU 核心数，以获得最佳性能。
         *
         * @return 核心线程数
         */
        private int getCorePoolSize() {
            return CPU_COUNT;
        }

        /**
         * 构建最终的 ThreadPoolExecutor 实例。
         *
         * @return ThreadPoolExecutor 实例
         */
        public ThreadPoolExecutor build() {
            return build(getCorePoolSize());
        }
    }

    /**
     * IO 密集型线程池构建器。
     */
    public static class IOThreadPoolBuilder extends AbstractThreadPoolBuilder<IOThreadPoolBuilder> {

        /**
         * 根据任务的 IO 等待时间与 CPU 计算时间的比率，计算核心线程数。
         * 公式：Ncpu * (1 + W/C)
         *
         * @param waitTime    任务中 IO 等待时间的估计值
         * @param computeTime 任务中 CPU 计算时间的估计值
         * @return 动态计算出的核心线程数
         */
        private int getCorePoolSize(int waitTime, int computeTime) {
            if (computeTime <= 0) {
                throw new IllegalArgumentException("CPU compute time must be greater than 0.");
            }
            return (int) (CPU_COUNT * (1 + (double) waitTime / computeTime));
        }

        /**
         * @param waitTime    任务中 IO 等待时间的估计值
         * @param computeTime 任务中 CPU 计算时间的估计值
         * @return ThreadPoolExecutor
         */
        public ThreadPoolExecutor build(int waitTime, int computeTime) {
            return build(getCorePoolSize(waitTime, computeTime));
        }
    }

}
