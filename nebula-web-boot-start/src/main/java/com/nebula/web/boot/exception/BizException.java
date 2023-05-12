package com.nebula.web.boot.exception;

import com.nebula.web.boot.api.IResultCode;
import com.nebula.web.boot.enums.ResultCode;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
public class BizException extends BaseException {

    private static final long serialVersionUID = 1L;


    public BizException(String errMessage) {
        super(ResultCode.FAILURE, errMessage);
    }

    public BizException(ResultCode errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public BizException(String errMessage, Throwable e) {
        super(errMessage, e);
    }

    public BizException(IResultCode errorCode, String errMessage, Throwable e) {
        super(errorCode, errMessage, e);
    }

}
