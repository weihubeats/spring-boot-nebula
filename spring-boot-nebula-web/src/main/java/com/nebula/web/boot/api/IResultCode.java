package com.nebula.web.boot.api;

import java.io.Serializable;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
public interface IResultCode extends Serializable {

    /**
     * 消息
     *
     * @return String
     */
    String getMessage();

    /**
     * 状态码
     *
     * @return int
     */
    int getCode();
}
