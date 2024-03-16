package com.nebula.distribute.lock.exception;

/**
 * @author : wh
 * @date : 2024/3/15 13:41
 * @description:
 */
public class DistributedLockException extends RuntimeException {

    public DistributedLockException(String message) {
        super(message);
    }
}
