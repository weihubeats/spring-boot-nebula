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
 
package com.nebula.web.boot.exception;

import com.nebula.web.boot.api.IErrorCode;
import com.nebula.web.boot.enums.ResultCode;
import lombok.Getter;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
@Getter
public abstract class BaseException extends RuntimeException implements IErrorCode {
    
    private final int code;
    
    private final Object[] args;
    
    public BaseException(IErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.args = args;
    }
    
    public BaseException(IErrorCode errorCode, String message, Object... args) {
        super(message);
        this.code = errorCode.getCode();
        this.args = args;
    }
    
    public BaseException(IErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.args = null;
    }
    
    public BaseException(String message) {
        this(ResultCode.FAILURE, message);
    }
    
    public BaseException(String message, Throwable cause) {
        this(ResultCode.FAILURE, message, cause);
    }
    
    @Override
    public String getMessage() {
        return super.getMessage();
    }
    
}
