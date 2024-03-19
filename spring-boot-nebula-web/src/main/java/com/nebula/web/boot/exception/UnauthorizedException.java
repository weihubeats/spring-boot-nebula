package com.nebula.web.boot.exception;

import com.nebula.web.boot.enums.ResultCode;

/**
 * @author : wh
 * @date : 2024/3/18 13:35
 * @description:
 */
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String errMessage) {
        super(ResultCode.UNAUTHORIZED, errMessage);
    }
}
