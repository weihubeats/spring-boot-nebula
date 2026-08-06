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
 
package com.nebula.web.boot.monitor;

/**
 * 告警消息载体，由监控编排器组装，由渠道实现渲染。
 * <p>堆栈以原始 {@link Throwable} 持有，格式化与长度限制由渠道内部处理。
 *
 * @param uri        请求路径
 * @param parameters 请求参数（JSON 字符串）
 * @param body       请求体（可能为空字符串）
 * @param cause      触发告警的异常
 */
public record AlertMessage(String uri, String parameters, String body, Throwable cause) {
    
}