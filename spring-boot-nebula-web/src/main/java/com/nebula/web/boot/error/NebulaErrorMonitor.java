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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 错误监控告警接口。
 * <p>实现类负责将异常信息推送至告警目标（飞书、钉钉等）。
 * 告警频率限制由外部共享的限流器统一管控，实现类不持有限流器。
 */
public interface NebulaErrorMonitor {
    
    /**
     * 监控并告警异常。
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  Spring MVC handler，通常为 HandlerMethod，代表触发异常的控制器方法
     * @param ex       捕获的异常
     */
    void monitorError(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);
}