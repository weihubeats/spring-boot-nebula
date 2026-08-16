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
 
package com.nebula.web.boot.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * {@link RepeatableReadFilter} 与 {@link RepeatableReadRequestWrapper} 测试：
 * 包装失败中止请求（413/400），请求体超限被拒绝，正常请求可重复读。
 */
class RepeatableReadFilterTest {
    
    @Test
    void oversizedBodyIsRejectedWith413AndNotPassedDown() throws ServletException, IOException {
        RepeatableReadFilter filter = new RepeatableReadFilter(10);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent("a-very-long-body-exceeding-limit".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        
        filter.doFilter(request, response, chain);
        
        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(chain.getRequest()).as("超限请求不得放行到下游").isNull();
    }
    
    @Test
    void wrapperFailureReturns400AndDoesNotContinueChain() throws ServletException, IOException {
        RepeatableReadFilter filter = new RepeatableReadFilter(1024);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            
            @Override
            public jakarta.servlet.ServletInputStream getInputStream() {
                throw new RuntimeException("boom");
            }
        };
        request.setContentType("application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        
        filter.doFilter(request, response, chain);
        
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(chain.getRequest()).as("包装失败后不得放行原请求").isNull();
    }
    
    @Test
    void normalBodyIsWrappedAndRepeatable() throws Exception {
        RepeatableReadFilter filter = new RepeatableReadFilter(1024);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        
        filter.doFilter(request, response, chain);
        
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isInstanceOf(RepeatableReadRequestWrapper.class);
        RepeatableReadRequestWrapper wrapper = (RepeatableReadRequestWrapper) chain.getRequest();
        byte[] first = wrapper.getInputStream().readAllBytes();
        byte[] second = wrapper.getInputStream().readAllBytes();
        assertThat(first).isEqualTo(second).isEqualTo("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void wrapperThrowsTooLargeForChunkedBodyExceedingLimit() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent(new byte[16]);
        
        assertThatThrownBy(() -> new RepeatableReadRequestWrapper(request, 8))
                .isInstanceOf(RequestBodyTooLargeException.class)
                .hasMessageContaining("8");
    }
}
