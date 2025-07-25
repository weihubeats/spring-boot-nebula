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

import com.nebula.base.utils.SingletonUtils;
import io.micrometer.core.instrument.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author : wh
 * @date : 2025/3/25
 * @description:
 */
public interface NebulaErrorMonitor {
    
    /**
     * 监控异常
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     */
    void monitorError(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);
    
    default String readUtf8String(String path) {
        return SingletonUtils.get("resource:" + path, () -> {
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path)) {
                if (inputStream == null) {
                    throw new IOException("Resource not found: " + path);
                }
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read resource: " + path, e);
            }
        });
        
    }
}
