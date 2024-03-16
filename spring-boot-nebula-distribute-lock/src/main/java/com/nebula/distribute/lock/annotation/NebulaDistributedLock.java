package com.nebula.distribute.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author : wh
 * @date : 2024/3/13 13:49
 * @description:
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NebulaDistributedLock {

    /**
     * 锁名字
     */
    String lockName() default "";

    /**
     * 锁前缀
     */
    String lockNamePre() default "";

    /**
     * 锁后缀
     */
    String lockNamePost() default "";

    /**
     * 锁前后缀拼接分隔符
     */
    String separator() default "_";

    /**
     * 是否使用公平锁
     */
    boolean fairLock() default false;

    /**
     * 是否使用尝试锁
     */
    boolean tryLock() default false;

    /**
     * 尝试锁最长等待时间
     */
    long tryWaitTime() default 30L;

    /**
     * 锁超时时间，超时自动释放锁
     */
    long outTime() default 20L;

    /**
     * 时间单位 默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
