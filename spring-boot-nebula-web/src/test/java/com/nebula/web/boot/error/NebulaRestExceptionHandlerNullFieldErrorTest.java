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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.config.NebulaWebProperties;
import com.nebula.web.common.utils.NebulaSysWebUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * {@link NebulaRestExceptionHandler} BindingResult 无 FieldError 时的安全兜底测试。
 */
class NebulaRestExceptionHandlerNullFieldErrorTest {
    
    private NebulaRestExceptionHandler newHandler() {
        Environment environment = new MockEnvironment();
        return new NebulaRestExceptionHandler(new NebulaSysWebUtils(environment), new NebulaWebProperties(), null);
    }
    
    @Test
    void handlesNullFieldErrorSafely() throws NoSuchMethodException {
        NebulaRestExceptionHandler handler = newHandler();
        
        MethodParameter parameter = new MethodParameter(
                NebulaRestExceptionHandlerNullFieldErrorTest.class.getDeclaredMethod("handlesNullFieldErrorSafely"), -1);
        BindingResult bindingResult = mock(BindingResult.class); // getFieldError() 默认返回 null
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        
        NebulaResponse<?> response = handler.handleError(new MockHttpServletRequest(), exception);
        
        assertThat(response).isNotNull();
        assertThat(response.getMsg()).isEqualTo("Parameter validation failed");
    }
}
