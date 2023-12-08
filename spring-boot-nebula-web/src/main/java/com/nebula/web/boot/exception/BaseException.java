package com.nebula.web.boot.exception;

import com.nebula.web.boot.api.IResultCode;
import com.nebula.web.boot.enums.ResultCode;
import lombok.Getter;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
@Getter public abstract class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final IResultCode code;

    public BaseException(String errMessage) {
        super(errMessage);
        this.code = ResultCode.INTERNAL_SERVER_ERROR;
    }

    public BaseException(IResultCode code, String errMessage) {
        super(errMessage);
        this.code = code;
    }

    public BaseException(String errMessage, Throwable e) {
        super(errMessage, e);
        this.code = ResultCode.FAILURE;
    }

    public BaseException(IResultCode code, String errMessage, Throwable e) {
        super(errMessage, e);
        this.code = code;
    }

}
