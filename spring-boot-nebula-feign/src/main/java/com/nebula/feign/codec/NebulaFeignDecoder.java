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

import com.nebula.web.boot.api.NebulaResponse;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Feign Decoder：将 HTTP 响应中的 {@link NebulaResponse} 自动解包为业务对象。
 * <p>
 * Feign 方法返回业务类型 {@code T} 时，按 {@code NebulaResponse<T>} 解码后调用 {@link NebulaResponse#data()}；
 * 若方法本身返回 {@link NebulaResponse}，则不做解包。
 */
public class NebulaFeignDecoder implements Decoder {
    
    private final Decoder delegate;
    
    public NebulaFeignDecoder(Decoder delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }
    
    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        if (!shouldUnwrap(type)) {
            return delegate.decode(response, type);
        }
        Object decoded = delegate.decode(response, wrapAsNebulaResponse(type));
        if (Objects.isNull(decoded)) {
            return null;
        }
        if (decoded instanceof NebulaResponse<?> nebulaResponse) {
            return nebulaResponse.data();
        }
        return decoded;
    }
    
    static boolean shouldUnwrap(Type type) {
        if (Objects.isNull(type)) {
            return false;
        }
        if (type instanceof Class<?> clazz) {
            return !isVoid(clazz) && !NebulaResponse.class.isAssignableFrom(clazz);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                return !NebulaResponse.class.isAssignableFrom(rawClass);
            }
        }
        return true;
    }
    
    private static boolean isVoid(Class<?> clazz) {
        return void.class.equals(clazz) || Void.class.equals(clazz);
    }
    
    static Type wrapAsNebulaResponse(Type bodyType) {
        return new ParameterizedType() {
            
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{bodyType};
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
    }
}
