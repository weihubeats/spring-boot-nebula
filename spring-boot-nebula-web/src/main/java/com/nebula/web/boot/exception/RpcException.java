package com.nebula.web.boot.exception;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
public class RpcException extends BaseException {

    public RpcException(String errMessage) {
        super(errMessage);
    }
}
