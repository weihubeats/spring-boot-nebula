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
 
package com.nebula.web.boot.error;

import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.config.NebulaWebProperties;
import com.nebula.web.boot.enums.ResultCode;
import com.nebula.web.boot.exception.BizException;
import com.nebula.web.boot.exception.RpcException;
import com.nebula.web.boot.exception.UnauthorizedException;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.nebula.web.common.utils.NebulaSysWebUtils;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author : wh
 * @date : 2024/3/18
 * @description:
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice
public class NebulaRestExceptionHandler {
    
    private final NebulaSysWebUtils nebulaSysWebUtils;
    
    private final NebulaWebProperties nebulaWebProperties;
    
    private final NebulaErrorMonitor nebulaErrorMonitor;
    
    public NebulaRestExceptionHandler(NebulaSysWebUtils nebulaSysWebUtils, NebulaWebProperties nebulaWebProperties,
                                      @Autowired(required = false) NebulaErrorMonitor nebulaErrorMonitor) {
        this.nebulaSysWebUtils = nebulaSysWebUtils;
        this.nebulaWebProperties = nebulaWebProperties;
        this.nebulaErrorMonitor = nebulaErrorMonitor;
        
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public NebulaResponse<String> handleError(HttpServletRequest request, MissingServletRequestParameterException e) {
        String message = String.format("Missing required request parameters: %s", e.getParameterName());
        return NebulaResponse.fail(ResultCode.PARAM_MISS, message);
    }
    
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public NebulaResponse<?> handleError(HttpServletRequest request, BindException e) {
        log.warn("Parameter binding failure: {}", e.getMessage());
        return handleError(request, e.getBindingResult());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public NebulaResponse<?> handleError(HttpServletRequest request, MethodArgumentNotValidException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        return handleError(request, e.getBindingResult());
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public NebulaResponse<?> handleError(HttpServletRequest request, UnauthorizedException e) {
        log.warn("Unauthorised requests: {}", e.getMessage());
        return NebulaResponse.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public NebulaResponse<?> handleError(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        log.error("Current request method not supported: {}", e.getMessage());
        return NebulaResponse.fail(ResultCode.METHOD_NOT_SUPPORTED, e.getMessage());
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public NebulaResponse<?> handleError(HttpServletRequest request, NoHandlerFoundException e) {
        log.error("404没找到请求:{}", e.getMessage());
        return NebulaResponse.fail(ResultCode.NOT_FOUND, e.getMessage());
    }
    
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public NebulaResponse<?> handleError(HttpServletRequest request, BizException e) {
        log.error("业务异常", e);
        return NebulaResponse.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(RpcException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public NebulaResponse<?> handleError(HttpServletRequest request, RpcException e) {
        log.error("RPC Exception", e);
        return NebulaResponse.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public NebulaResponse<?> methodArgumentTypeMismatchException(HttpServletRequest request,
                                                                 MethodArgumentTypeMismatchException e) {
        log.error("参数类型异常 ", e);
        return NebulaResponse.fail(ResultCode.PARAM_BIND_ERROR);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public NebulaResponse<?> httpMessageNotReadableExceptionHandle(HttpServletRequest request,
                                                                   HttpMessageNotReadableException e) {
        log.error("数据格式错误 ", e);
        return NebulaResponse.fail(ResultCode.PARAM_BIND_ERROR, "数据格式错误");
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public NebulaResponse<?> defaultErrorHandle(HttpServletRequest request, HttpServletResponse response,
                                                Object handler, Exception ex) {
        NebulaResponse<?> baseResponse = new NebulaResponse<>();
        baseResponse.setCode(ResultCode.INTERNAL_SERVER_ERROR.getCode());
        if (nebulaSysWebUtils.isPrd() && nebulaWebProperties.isMonitorOpen() && !Objects.isNull(nebulaErrorMonitor)) {
            nebulaErrorMonitor.monitorError(request, response, handler, ex);
            baseResponse.setMsg("Server busy");
        } else {
            baseResponse.setMsg("错误消息:" + ex.getMessage());
        }
        log.error("server error ", ex);
        return baseResponse;
    }
    
    private NebulaResponse<?> handleError(HttpServletRequest request, BindingResult result) {
        FieldError error = result.getFieldError();
        assert error != null;
        String message = String.format("%s:%s", error.getField(), error.getDefaultMessage());
        return NebulaResponse.fail(ResultCode.PARAM_BIND_ERROR, message);
    }
    
}
