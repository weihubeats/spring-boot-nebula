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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
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
    
    private static final String LUA_SCRIPT =
            "local k = KEYS[1]\n" +
                    "local now = tonumber(ARGV[1])\n" +
                    "local window = tonumber(ARGV[2])\n" +
                    "local expire = tonumber(ARGV[3])\n" +
                    "local maxCount = tonumber(ARGV[4])\n" +
                    "local member = ARGV[5]\n" +
                    "redis.call('ZREMRANGEBYSCORE', k, 0, now - window)\n" +
                    "local count = redis.call('ZCARD', k)\n" +
                    "if count >= maxCount then\n" +
                    "  return 0\n" +
                    "end\n" +
                    "redis.call('ZADD', k, now, member)\n" +
                    "redis.call('PEXPIRE', k, expire * 1000)\n" +
                    "return 1";
    
    private final RScript lua;
    private final String scriptId;
    
    public RedisNebulaAlertLimiter(RedissonClient redissonClient, Duration window, Duration expire,
                                   int maxCount, String keyPrefix) {
        if (maxCount <= 0) {
            throw new IllegalArgumentException("nebula.web.monitor-limit-max-count must be positive");
        }
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("nebula.web.monitor-limit-window-seconds must be positive");
        }
        if (expire == null || expire.isZero() || expire.isNegative()) {
            throw new IllegalArgumentException("nebula.web.monitor-limit-expire-seconds must be positive");
        }
        if (expire.compareTo(window) < 0) {
            throw new IllegalArgumentException("nebula.web.monitor-limit-expire-seconds must be at least the window");
        }
        this.redissonClient = redissonClient;
        this.window = window;
        this.expire = expire;
        this.maxCount = maxCount;
        this.keyPrefix = keyPrefix;
        this.lua = redissonClient.getScript();
        this.scriptId = this.lua.scriptLoad(LUA_SCRIPT);
    }
    
    @Override
    public boolean tryAcquire(String key) {
        String fullKey = keyPrefix + key;
        long now = System.currentTimeMillis();
        String member = now + ":" + System.nanoTime();
        
        try {
            @SuppressWarnings("unchecked")
            Long result = (Long) lua.evalSha(Mode.READ_WRITE, scriptId, ReturnType.INTEGER,
                    List.of(fullKey), now, (long) window.toMillis(),
                    (long) expire.toMillis(), (long) maxCount, member);
            boolean allowed = result == 1L;
            if (!allowed && log.isDebugEnabled()) {
                log.debug("Redis 限流: key={}, limit={}", fullKey, maxCount);
            }
            return allowed;
        } catch (Exception e) {
            log.warn("Redis 限流失败, 降级放行, key={}, error={}", fullKey, e.getMessage());
            return true;
        }
    }
}
