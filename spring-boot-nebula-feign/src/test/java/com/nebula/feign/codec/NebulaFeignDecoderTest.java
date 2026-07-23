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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.enums.ResultCode;
import com.nebula.web.boot.exception.BizException;
import com.nebula.web.boot.exception.RpcException;
import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author : wh
 * @date : 2026/7/22
 * @description: NebulaFeignDecoder 单元测试
 */
class NebulaFeignDecoderTest {
    
    private Decoder delegate;
    private NebulaFeignDecoder decoder;
    
    @BeforeEach
    void setUp() {
        delegate = mock(Decoder.class);
        decoder = new NebulaFeignDecoder(delegate);
    }
    
    @Test
    void shouldUnwrapBusinessType() throws Exception {
        NebulaResponse<String> wrapped = NebulaResponse.data(200, "小奏", "success");
        when(delegate.decode(any(Response.class), any(Type.class))).thenReturn(wrapped);
        
        Object result = decoder.decode(emptyResponse(), String.class);
        
        assertEquals("小奏", result);
    }
    
    @Test
    void shouldPassThroughNebulaResponseType() throws Exception {
        NebulaResponse<String> wrapped = NebulaResponse.data(200, "小奏", "success");
        Type nebulaResponseType = new ParameterizedType() {
            
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }
            
            @Override
            public Type getRawType() {
                return NebulaResponse.class;
            }
            
            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        when(delegate.decode(any(Response.class), eq(nebulaResponseType))).thenReturn(wrapped);
        
        Object result = decoder.decode(emptyResponse(), nebulaResponseType);
        
        assertEquals(wrapped, result);
    }
    
    @Test
    void shouldThrowBizExceptionWhenBizCode() throws Exception {
        NebulaResponse<String> wrapped = NebulaResponse.fail(ResultCode.BIZ_EXCEPTION, "业务失败");
        when(delegate.decode(any(Response.class), any(Type.class))).thenReturn(wrapped);
        
        assertThrows(BizException.class, () -> decoder.decode(emptyResponse(), String.class));
    }
    
    @Test
    void shouldThrowRpcExceptionWhenNotSuccess() throws Exception {
        // FAILURE(400) 与 BIZ_EXCEPTION(500) 不同，走 RpcException 分支
        NebulaResponse<String> wrapped = NebulaResponse.fail(ResultCode.FAILURE, "rpc 失败");
        when(delegate.decode(any(Response.class), any(Type.class))).thenReturn(wrapped);
        
        assertThrows(RpcException.class, () -> decoder.decode(emptyResponse(), String.class));
    }
    
    @Test
    void shouldUnwrapDetectsTypes() {
        assertTrue(NebulaFeignDecoder.shouldUnwrap(String.class));
        assertFalse(NebulaFeignDecoder.shouldUnwrap(NebulaResponse.class));
        assertFalse(NebulaFeignDecoder.shouldUnwrap(void.class));
        assertFalse(NebulaFeignDecoder.shouldUnwrap(Void.class));
    }
    
    private static Response emptyResponse() {
        return Response.builder()
                .status(200)
                .reason("OK")
                .request(Request.create(Request.HttpMethod.GET, "/test", Collections.emptyMap(), null,
                        StandardCharsets.UTF_8))
                .headers(Collections.emptyMap())
                .build();
    }
}
