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
 
package com.nebula.feign.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nebula.base.utils.JsonUtil;
import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.enums.ResultCode;
import com.nebula.web.boot.exception.BizException;
import com.nebula.web.boot.exception.RpcException;
import feign.FeignException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * NebulaFeignErrorDecoder 单元测试。
 */
class NebulaFeignErrorDecoderTest {
    
    private NebulaFeignErrorDecoder errorDecoder;
    
    @BeforeEach
    void setUp() {
        errorDecoder = new NebulaFeignErrorDecoder();
    }
    
    @Test
    void shouldThrowBizExceptionWhenBizCode() {
        NebulaResponse<String> body = NebulaResponse.fail(ResultCode.BIZ_EXCEPTION, "业务失败");
        Exception ex = errorDecoder.decode("UserClient#getUser()", errorResponse(500, body));
        
        assertInstanceOf(BizException.class, ex);
        assertEquals("业务失败", ex.getMessage());
    }
    
    @Test
    void shouldThrowRpcExceptionWhenFailureCode() {
        NebulaResponse<String> body = NebulaResponse.fail(ResultCode.FAILURE, "rpc 失败");
        Exception ex = errorDecoder.decode("UserClient#getUser()", errorResponse(400, body));
        
        assertInstanceOf(RpcException.class, ex);
        assertEquals("rpc 失败", ex.getMessage());
    }
    
    @Test
    void shouldFallbackWhenBodyEmpty() {
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(), null,
                        StandardCharsets.UTF_8))
                .headers(Collections.emptyMap())
                .build();
        
        Exception ex = errorDecoder.decode("UserClient#getUser()", response);
        
        assertTrue(ex instanceof FeignException);
    }
    
    @Test
    void shouldFallbackWhenBodyNotNebulaResponse() {
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(), null,
                        StandardCharsets.UTF_8))
                .headers(Collections.emptyMap())
                .body("not-json", StandardCharsets.UTF_8)
                .build();
        
        Exception ex = errorDecoder.decode("UserClient#getUser()", response);
        
        assertTrue(ex instanceof FeignException);
    }
    
    private Response errorResponse(int status, NebulaResponse<?> body) {
        byte[] bytes = JsonUtil.toJsonBytes(body);
        return Response.builder()
                .status(status)
                .reason("ERROR")
                .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(), null,
                        StandardCharsets.UTF_8))
                .headers(Collections.emptyMap())
                .body(bytes)
                .build();
    }
}
