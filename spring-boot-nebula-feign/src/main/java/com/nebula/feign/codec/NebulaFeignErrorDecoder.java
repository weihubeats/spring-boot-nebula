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

import com.nebula.base.utils.JsonUtil;
import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.exception.BizException;
import com.nebula.web.boot.exception.RpcException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Feign ErrorDecoder：解析非 2xx 响应体中的 {@link NebulaResponse}，按业务码抛出异常。
 * <p>
 * 与 {@link NebulaResponse#data()} 规则一致：业务异常码 → {@link BizException}，其余失败码 → {@link RpcException}。
 * 无法解析时回退到 Feign 默认 ErrorDecoder。
 */
public class NebulaFeignErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        if (Objects.isNull(response) || Objects.isNull(response.body())) {
            return defaultDecoder.decode(methodKey, response);
        }
        byte[] bodyData;
        try (InputStream inputStream = response.body().asInputStream()) {
            bodyData = inputStream.readAllBytes();
        } catch (IOException ex) {
            return defaultDecoder.decode(methodKey, response);
        }
        Response buffered = response.toBuilder().body(bodyData).build();
        if (bodyData.length == 0) {
            return defaultDecoder.decode(methodKey, buffered);
        }
        NebulaResponse<?> nebulaResponse = JsonUtil.fromJson(bodyData, NebulaResponse.class);
        if (Objects.isNull(nebulaResponse)) {
            return defaultDecoder.decode(methodKey, buffered);
        }
        try {
            nebulaResponse.data();
        } catch (BizException | RpcException ex) {
            return ex;
        }
        return defaultDecoder.decode(methodKey, buffered);
    }
}
