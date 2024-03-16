package com.nebula.distribute.lock.core;

import java.util.concurrent.TimeUnit;

/**
 * @author : wh
 * @date : 2024/3/15 13:35
 * @description:
 */
public interface NebulaDistributedLockTemplate {

    /**
     * 默认超时锁释放时间
     */
    long DEFAULT_OUT_TIME = 5;
    /**
     * 默认尝试加锁时间
     */
    long DEFAULT_TRY_OUT_TIME   = 30;
    /**
     * 默认时间单位
     */
    TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 加锁
     * @param distributedLock
     * @param fairLock 是否使用公平锁
     * @param <T>
     * @return
     */
    <T> T lock(DistributedLock<T> distributedLock, boolean fairLock);

    /**
     *
     * @param distributedLock
     * @param outTime 锁超时时间。超时后自动释放锁
     * @param timeUnit 时间单位
     * @param fairLock 是否使用公平锁
     * @param <T>
     * @return
     */
    <T> T lock(DistributedLock<T> distributedLock, long outTime, TimeUnit timeUnit, boolean fairLock);

    /**
     * 尝试加锁
     * @param distributedLock
     * @param fairLock 是否使用公平锁
     * @param <T>
     * @return
     */
    <T> T tryLock(DistributedLock<T> distributedLock, boolean fairLock);

    /**
     *
     * @param distributedLock
     * @param tryOutTime 尝试获取锁时间
     * @param outTime 锁超时时间
     * @param timeUnit 时间单位
     * @param fairLock 是否使用公平锁
     * @param <T>
     * @return
     */
    <T> T tryLock(DistributedLock<T> distributedLock, long tryOutTime, long outTime, TimeUnit timeUnit, boolean fairLock);


}
