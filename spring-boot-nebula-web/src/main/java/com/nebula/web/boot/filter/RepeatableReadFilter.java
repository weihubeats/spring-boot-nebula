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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RepeatableReadFilter extends OncePerRequestFilter {
    
    private final int maxCachedBodyBytes;
    
    public RepeatableReadFilter() {
        this(RepeatableReadRequestWrapper.DEFAULT_MAX_CACHED_BODY_BYTES);
    }
    
    public RepeatableReadFilter(int maxCachedBodyBytes) {
        this.maxCachedBodyBytes = maxCachedBodyBytes;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String contentType = request.getContentType();
        
        // 1. 如果是文件上传类型，直接放行，千万不要去包装
        if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 2. 包装 Request。包装失败时原始输入流可能已被部分消费，放行原 request 会导致下游读到残缺 body，
        // 因此必须中止请求而不是继续过滤器链
        RepeatableReadRequestWrapper requestWrapper;
        try {
            requestWrapper = new RepeatableReadRequestWrapper(request, maxCachedBodyBytes);
        } catch (RequestBodyTooLargeException e) {
            log.warn("Nebula SDK: 请求体超过缓存上限({} bytes), 拒绝请求: uri={}", maxCachedBodyBytes, request.getRequestURI());
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, e.getMessage());
            return;
        } catch (Exception e) {
            log.warn("Nebula SDK: 包装请求体失败, 中止请求: uri={}", request.getRequestURI(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request body");
            return;
        }
        
        // 3. 将包装后的 request 往下传递
        filterChain.doFilter(requestWrapper, response);
    }
}
