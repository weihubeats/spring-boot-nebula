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
 
package com.nebula.distribute.lock.core;

import com.nebula.distribute.lock.exception.DistributedLockException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @author : wh
 * @date : 2024/3/15 13:36
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonDistributedLockTemplate implements NebulaDistributedLockTemplate {
    
    private final RedissonClient redisson;
    
    @Override
    public <T> T lock(DistributedLock<T> distributedLock, boolean fairLock) {
        return lock(distributedLock, DEFAULT_OUT_TIME, DEFAULT_TIME_UNIT, fairLock);
    }
    
    @Override
    public <T> T lock(DistributedLock<T> distributedLock, long outTime, TimeUnit timeUnit, boolean fairLock) {
        RLock lock = getLock(distributedLock.lockName(), fairLock);
        lock.lock(outTime, timeUnit);
        try {
            return distributedLock.process();
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }
    
    @Override
    public <T> T tryLock(DistributedLock<T> distributedLock, boolean fairLock) {
        return tryLock(distributedLock, DEFAULT_TRY_OUT_TIME, DEFAULT_OUT_TIME, DEFAULT_TIME_UNIT, fairLock);
    }
    
    @Override
    public <T> T tryLock(DistributedLock<T> distributedLock, long tryOutTime, long outTime, TimeUnit timeUnit,
                         boolean fairLock) {
        String lockName = distributedLock.lockName();
        RLock lock = getLock(lockName, fairLock);
        try {
            log.info("try acquire lock {}", lockName);
            if (lock.tryLock(tryOutTime, outTime, timeUnit)) {
                log.info("lock acquired {}", lockName);
                try {
                    return distributedLock.process();
                } finally {
                    // isHeldByCurrentThread 防止锁过期再释放锁导致报错
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.info("lock released {}", lockName);
                    }
                }
            }
        } catch (InterruptedException ignored) {
            log.warn("锁中断...");
            log.info("can not acquire lock {}", lockName);
        }
        throw new DistributedLockException("lock fail");
    }
    
    private RLock getLock(String lockName, boolean fairLock) {
        return fairLock ? redisson.getFairLock(lockName) : redisson.getLock(lockName);
    }
    
}
