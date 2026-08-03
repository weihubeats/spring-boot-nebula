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

/**
 * 告警频率限制抽象。
 * <p>实现类由 Spring 管理生命周期，所有告警实现（飞书、钉钉等）共享同一个限流器实例。
 */
public interface NebulaAlertLimiter {
    
    /**
     * 尝试获取告警配额。
     *
     * @param key 告警维度，通常为 异常类名:归一化URI
     * @return true=配额内可发送告警；false=已达窗口上限应丢弃
     */
    boolean tryAcquire(String key);
}