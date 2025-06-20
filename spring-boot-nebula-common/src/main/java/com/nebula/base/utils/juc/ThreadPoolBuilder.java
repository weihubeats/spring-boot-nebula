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

import com.nebula.base.utils.SystemUtil;
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
    
    private static final RejectedExecutionHandler defaultRejectHandler = new ThreadPoolExecutor.AbortPolicy();
    
    /**
     * cpu核数
     */
    private static final int CPU = SystemUtil.getCPU();
    
    /**
     * create io ThreadPoolExecutor
     *
     * @return ThreadPoolExecutor
     */
    public static IOThreadPoolBuilder ioThreadPoolBuilder() {
        return new IOThreadPoolBuilder();
    }
    
    /**
     * create cpu ThreadPoolExecutor
     *
     * @return ThreadPoolExecutor
     */
    public static CPUThreadPoolBuilder cpuThreadPoolBuilder() {
        return new CPUThreadPoolBuilder();
    }
    
    /**
     * IO 类型线程池
     */
    public static class IOThreadPoolBuilder {
        
        private ThreadFactory threadFactory;
        
        private RejectedExecutionHandler rejectHandler;
        
        private int queueSize = -1;
        
        private int maximumPoolSize = CPU;
        
        private int keepAliveTime = 120;
        
        private boolean daemon = false;
        
        private String threadNamePrefix;
        
        public int getCorePooSize(int ioTime, int cpuTime) {
            return CPU + (1 + (ioTime / cpuTime));
        }
        
        public IOThreadPoolBuilder setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
            return this;
        }
        
        public IOThreadPoolBuilder setDaemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }
        
        public IOThreadPoolBuilder setRejectHandler(RejectedExecutionHandler rejectHandler) {
            this.rejectHandler = rejectHandler;
            return this;
        }
        
        public IOThreadPoolBuilder setQueueSize(int queueSize) {
            this.queueSize = queueSize;
            return this;
        }
        
        public IOThreadPoolBuilder setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            return this;
        }
        
        public IOThreadPoolBuilder setKeepAliveTime(int keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
            return this;
        }
        
        public ThreadPoolExecutor builder(int ioTime, int cpuTime) {
            BlockingQueue<Runnable> queue;
            
            if (rejectHandler == null) {
                rejectHandler = defaultRejectHandler;
            }
            threadFactory = new ThreadFactoryImpl(this.threadNamePrefix, this.daemon);
            
            queue = queueSize < 1 ? new LinkedBlockingQueue<>() : new ArrayBlockingQueue<>(queueSize);
            
            return new ThreadPoolExecutor(getCorePooSize(ioTime, cpuTime), maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, queue, threadFactory, rejectHandler);
            
        }
        
    }
    
    /**
     * CPU 类型线程池
     */
    public static class CPUThreadPoolBuilder {
        
        private ThreadFactory threadFactory;
        
        private RejectedExecutionHandler rejectHandler;
        
        private int queueSize = -1;
        
        private int maximumPoolSize = CPU;
        
        private int keepAliveTime = 120;
        
        private boolean daemon = false;
        
        private String threadNamePrefix;
        
        public int getCorePooSize() {
            return CPU;
        }
        
        public CPUThreadPoolBuilder setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
            return this;
        }
        
        public CPUThreadPoolBuilder setDaemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }
        
        public CPUThreadPoolBuilder setRejectHandler(RejectedExecutionHandler rejectHandler) {
            this.rejectHandler = rejectHandler;
            return this;
        }
        
        public CPUThreadPoolBuilder setQueueSize(int queueSize) {
            this.queueSize = queueSize;
            return this;
        }
        
        public CPUThreadPoolBuilder setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            return this;
        }
        
        public CPUThreadPoolBuilder setKeepAliveTime(int keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
            return this;
        }
        
        public ThreadPoolExecutor builder() {
            if (rejectHandler == null) {
                rejectHandler = defaultRejectHandler;
            }
            threadFactory = new ThreadFactoryImpl(this.threadNamePrefix, this.daemon);
            
            BlockingQueue<Runnable> queue = queueSize < 1 ? new LinkedBlockingQueue<>() : new ArrayBlockingQueue<>(queueSize);
            
            return new ThreadPoolExecutor(getCorePooSize(), maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, queue, threadFactory, rejectHandler);
            
        }
        
    }
    
}
