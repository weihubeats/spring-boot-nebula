/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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
