package com.nebula.web.boot.enums;

import com.nebula.web.boot.api.IResultCode;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : wh
 * @date : 2021/12/22 14:37
 * @description: 业务代码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode implements IResultCode {

    /**
     * 操作成功
     */
    SUCCESS(HttpServletResponse.SC_OK, "success"),
    PARAM_MISS(HttpServletResponse.SC_BAD_REQUEST, "缺少必要的请求参数"),
    UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "未授权"),
    METHOD_NOT_SUPPORTED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "不支持当前请求方法"),
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "404 没找到请求"),
    FAILURE(HttpServletResponse.SC_BAD_REQUEST, "业务异常"),
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器异常"),
    PARAM_BIND_ERROR(HttpServletResponse.SC_BAD_REQUEST, "请求参数绑定错误"),
    BIZ_EXCEPTION(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "业务异常");


    /**
     * code编码
     */
    final int code;
    /**
     * 中文信息描述
     */
    final String message;






}

