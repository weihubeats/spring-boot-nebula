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
 
package com.nebula.web.boot.error;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

/**
 * Redis 滑动窗口限流器（基于 Redisson Sorted Set）。
 * <p>使用 ZREMRANGEBYSCORE + ZCARD + ZADD 实现滑动窗口计数。
 * <p>Redis 不可用时降级放行（fail-open），避免告警系统因 Redis 故障而阻塞。
 */
@Slf4j
public class RedisNebulaAlertLimiter implements NebulaAlertLimiter {
    
    private final RedissonClient redissonClient;
    private final Duration window;
    private final Duration expire;
    private final int maxCount;
    private final String keyPrefix;
    
    public RedisNebulaAlertLimiter(RedissonClient redissonClient, Duration window, Duration expire,
                                   int maxCount, String keyPrefix) {
        if (maxCount <= 0) {
            throw new IllegalArgumentException("nebula.web.monitor-limit-max-count must be positive");
        }
        this.redissonClient = redissonClient;
        this.window = window;
        this.expire = expire;
        this.maxCount = maxCount;
        this.keyPrefix = keyPrefix;
    }
    
    @Override
    public boolean tryAcquire(String key) {
        String fullKey = keyPrefix + key;
        long now = System.currentTimeMillis();
        String member = now + ":" + System.nanoTime();
        
        try {
            RScoredSortedSet<String> set = redissonClient.getScoredSortedSet(fullKey);
            long windowMs = window.toMillis();
            set.removeRangeByScore(0.0, true, (double) (now - windowMs), false);
            long count = set.size();
            if (count >= maxCount) {
                if (log.isDebugEnabled()) {
                    log.debug("Redis 限流: key={}, count={}, limit={}", fullKey, count, maxCount);
                }
                return false;
            }
            set.add(now, member);
            set.expire(expire);
            return true;
        } catch (Exception e) {
            log.warn("Redis 限流失败, 降级放行, key={}, error={}", fullKey, e.getMessage());
            return true;
        }
    }
}
