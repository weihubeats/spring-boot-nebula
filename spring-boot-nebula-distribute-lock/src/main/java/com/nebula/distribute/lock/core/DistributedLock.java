package com.nebula.distribute.lock.core;

/**
 * @author : wh
 * @date : 2024/3/15 13:35
 * @description:
 */
public interface DistributedLock<T> {

    /**
     * 分布式锁逻辑 代码块
     */
    T process();

    String lockName();
}
