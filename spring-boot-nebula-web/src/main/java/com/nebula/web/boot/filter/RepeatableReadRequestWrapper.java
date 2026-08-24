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

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {
    
    /**
     * 默认请求体缓存上限 1MB，与 {@code nebula.web.max-cached-request-body-bytes} 默认值一致
     */
    public static final int DEFAULT_MAX_CACHED_BODY_BYTES = 1024 * 1024;
    
    private final byte[] bodyCache;
    // 标记当前请求是否被缓存。如果是不支持缓存的类型（如文件上传），则设为 false
    private final boolean isCacheable;
    
    public RepeatableReadRequestWrapper(HttpServletRequest request) throws IOException {
        this(request, DEFAULT_MAX_CACHED_BODY_BYTES);
    }
    
    public RepeatableReadRequestWrapper(HttpServletRequest request, int maxCachedBodyBytes) throws IOException {
        super(request);
        
        String contentType = request.getContentType();
        // 跳过大文件/表单上传类型的缓存，防止 OOM
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            this.isCacheable = false;
            this.bodyCache = new byte[0];
        } else {
            this.isCacheable = true;
            // 只有普通的文本/JSON请求才将其完整读取到内存，超过上限直接拒绝（超限时输入流已被部分消费，
            // 原请求无法再安全放行，故必须由 Filter 中止请求）
            long contentLength = request.getContentLengthLong();
            if (contentLength > maxCachedBodyBytes) {
                throw new RequestBodyTooLargeException(maxCachedBodyBytes);
            }
            this.bodyCache = readBounded(request, maxCachedBodyBytes);
        }
    }
    
    private static byte[] readBounded(HttpServletRequest request, int maxCachedBodyBytes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream in = request.getInputStream();
        byte[] buf = new byte[8192];
        int total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            total += n;
            if (total > maxCachedBodyBytes) {
                throw new RequestBodyTooLargeException(maxCachedBodyBytes);
            }
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 如果不支持缓存，直接返回原生的流，避免破坏文件上传等功能
        if (!isCacheable) {
            return super.getInputStream();
        }
        
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bodyCache);
        
        return new ServletInputStream() {
            
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }
            
            @Override
            public boolean isReady() {
                return true;
            }
            
            @Override
            public void setReadListener(ReadListener readListener) {
                // 空实现
            }
            
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        // 如果不支持缓存，返回原生 Reader
        if (!isCacheable) {
            return super.getReader();
        }
        
        // 处理可能为空的字符编码，提供 UTF-8 兜底
        String encoding = getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            encoding = StandardCharsets.UTF_8.name();
        }
        
        return new BufferedReader(new InputStreamReader(this.getInputStream(), encoding));
    }
}