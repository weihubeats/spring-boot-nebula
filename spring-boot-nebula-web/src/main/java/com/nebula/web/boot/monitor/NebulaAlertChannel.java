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
 * 告警渠道抽象。
 * <p>实现类负责将 {@link AlertMessage} 渲染并推送至具体渠道（飞书、钉钉等），
 * 渠道自身的长度限制、模板渲染由实现类内部处理。
 */
public interface NebulaAlertChannel {
    
    /**
     * 发送告警消息。
     *
     * @param message 由监控编排器组装的消息
     */
    void send(AlertMessage message);
}
