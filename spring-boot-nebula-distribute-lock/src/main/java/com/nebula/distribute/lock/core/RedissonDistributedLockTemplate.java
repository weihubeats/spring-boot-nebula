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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
    
    private final Cache<String, RLock> lockCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .weakValues()
            .build();
    
    @Override
    public <T> T lock(DistributedLock<T> distributedLock, boolean fairLock, boolean watchDogEnabled) throws Throwable {
        return lock(distributedLock, DEFAULT_OUT_TIME, DEFAULT_TIME_UNIT, fairLock, watchDogEnabled);
    }
    
    @Override
    public <T> T lock(DistributedLock<T> distributedLock, long outTime, TimeUnit timeUnit,
                      boolean fairLock, boolean watchDogEnabled) throws Throwable {
        String lockName = distributedLock.lockName();
        RLock lock = getLock(lockName, fairLock);
        acquireLock(lock, lockName, watchDogEnabled, outTime, timeUnit);
        log.debug("Lock acquired: {}", lockName);
        try {
            return distributedLock.process();
        } catch (Throwable e) {
            log.error("Error while executing locked process: {}", lockName, e);
            throw e;
        } finally {
            tryUnlock(lock, lockName);
        }
    }
    
    @Override
    public <T> T tryLock(DistributedLock<T> distributedLock, boolean fairLock, boolean watchDogEnabled) throws Throwable {
        return tryLock(distributedLock, DEFAULT_TRY_OUT_TIME, DEFAULT_OUT_TIME, DEFAULT_TIME_UNIT, fairLock, watchDogEnabled);
    }
    
    @Override
    public <T> T tryLock(DistributedLock<T> distributedLock, long tryOutTime, long outTime,
                         TimeUnit timeUnit, boolean fairLock, boolean watchDogEnabled) throws Throwable {
        String lockName = distributedLock.lockName();
        RLock lock = getLock(lockName, fairLock);
        try {
            log.debug("Trying to acquire lock: {} (wait: {}s, timeout: {}s, watchDog: {})",
                    lockName, tryOutTime, outTime, watchDogEnabled);
            boolean acquired = tryAcquireLock(lock, lockName, watchDogEnabled, tryOutTime, outTime, timeUnit);
            if (acquired) {
                log.debug("Lock acquired: {}", lockName);
                try {
                    return distributedLock.process();
                } catch (Throwable e) {
                    log.error("Error while executing locked process: {}", lockName, e);
                    throw e;
                } finally {
                    tryUnlock(lock, lockName);
                }
            } else {
                log.warn("Failed to acquire lock: {} after {}s", lockName, tryOutTime);
                throw new DistributedLockException("Failed to acquire lock: " + lockName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted: {}", lockName, e);
            throw new DistributedLockException("Lock acquisition interrupted: " + lockName, e);
        } catch (Exception e) {
            log.error("Error while acquiring lock: {}", lockName, e);
            throw new DistributedLockException("Error while acquiring lock: " + lockName, e);
        }
    }
    
    private void acquireLock(RLock lock, String lockName, boolean watchDogEnabled,
                             long outTime, TimeUnit timeUnit) throws InterruptedException {
        if (watchDogEnabled) {
            log.debug("Acquiring lock with watch-dog enabled: {}", lockName);
            lock.lock();
        } else {
            log.debug("Acquiring lock: {} (lease: {} {})", lockName, outTime, timeUnit);
            lock.lock(outTime, timeUnit);
        }
    }
    
    private boolean tryAcquireLock(RLock lock, String lockName, boolean watchDogEnabled,
                                   long tryOutTime, long outTime, TimeUnit timeUnit) throws InterruptedException {
        if (watchDogEnabled) {
            return lock.tryLock(tryOutTime, timeUnit);
        }
        return lock.tryLock(tryOutTime, outTime, timeUnit);
    }
    
    /**
     * 安全解锁。仅在锁被当前线程持有时释放，避免 IllegalMonitorStateException。
     * 当锁因 lease 过期被 Redis 自动释放时，isHeldByCurrentThread() 返回 false，
     * 此时跳过 unlock() 是正确的行为。
     */
    private void tryUnlock(RLock lock, String lockName) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Lock released: {}", lockName);
        } else {
            log.warn("Lock {} expired before business logic finished; mutual exclusion may have been broken. "
                    + "Consider a longer lease time or enabling the watch-dog.", lockName);
        }
    }
    
    /**
     * 获取锁对象，使用 Caffeine 缓存提高性能并防止内存泄漏
     */
    private RLock getLock(String lockName, boolean fairLock) {
        String cacheKey = (fairLock ? "fair:" : "unfair:") + lockName;
        return lockCache.get(cacheKey, k -> fairLock ? redisson.getFairLock(lockName) : redisson.getLock(lockName));
    }
}
