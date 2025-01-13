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
 
package com.nebula.web.boot.api;

import com.nebula.web.boot.enums.ResultCode;
import com.nebula.web.boot.exception.BizException;
import com.nebula.web.boot.exception.RpcException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
@NoArgsConstructor
@Getter
@Setter
public class NebulaResponse<T> implements Serializable {
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 返回消息
     */
    private String msg;
    
    private NebulaResponse(IResultCode resultCode) {
        this(resultCode, null, resultCode.getMessage());
    }
    
    private NebulaResponse(IResultCode resultCode, String msg) {
        this(resultCode, null, msg);
    }
    
    private NebulaResponse(IResultCode resultCode, T data) {
        this(resultCode, data, resultCode.getMessage());
    }
    
    private NebulaResponse(IResultCode resultCode, T data, String msg) {
        this(resultCode.getCode(), data, msg);
    }
    
    private NebulaResponse(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }
    
    /**
     * 请求是否成功
     * @param result
     * @return
     */
    public static boolean isSuccess(@Nullable NebulaResponse<?> result) {
        return Optional.ofNullable(result)
                .map(x -> Objects.equals(ResultCode.SUCCESS.getCode(), x.code))
                .orElse(Boolean.FALSE);
    }
    
    /**
     * 获取 data
     * @return
     */
    public T data() {
        // 如果是业务异常
        if (isBizException(this)) {
            throw new BizException(this.msg);
        }
        if (isNotSuccess(this)) {
            throw new RpcException(this.msg);
        }
        return this.data;
    }
    
    private boolean isBizException(NebulaResponse<T> NebulaResponse) {
        return Optional.ofNullable(NebulaResponse)
                .map(x -> Objects.equals(ResultCode.BIZ_EXCEPTION.getCode(), x.code))
                .orElse(Boolean.FALSE);
    }
    
    /**
     * 请求是否失败
     * @param result
     * @return
     */
    public static boolean isNotSuccess(@Nullable NebulaResponse<?> result) {
        return !NebulaResponse.isSuccess(result);
    }
    
    /**
     * 返回R
     *
     * @param data 数据
     * @param msg  消息
     * @param <T>  T 泛型标记
     * @return R
     */
    public static <T extends Serializable> NebulaResponse<T> data(T data, String msg) {
        return data(HttpServletResponse.SC_OK, data, msg);
    }
    
    /**
     * 返回R
     *
     * @param code 状态码
     * @param data 数据
     * @param msg  消息
     * @param <T>  T 泛型标记
     * @return R
     */
    public static <T extends Serializable> NebulaResponse<T> data(int code, T data, String msg) {
        return new NebulaResponse<>(code, data, data == null ? "no data" : msg);
    }
    
    public static <T> NebulaResponse<T> fail(IResultCode resultCode, String msg) {
        return new NebulaResponse<>(resultCode, msg);
    }
    
    public static <T> NebulaResponse<T> fail(IResultCode resultCode) {
        return new NebulaResponse<>(resultCode, resultCode.getMessage());
    }
    
    /**
     * 返回R
     *
     * @param msg 消息
     * @param <T> T 泛型标记
     * @return R
     */
    public static <T> NebulaResponse<T> fail(String msg) {
        return new NebulaResponse<>(ResultCode.FAILURE, msg);
    }
    
}
