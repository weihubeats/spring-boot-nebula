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
 
package com.nebula.i18n.remote;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 远程文案加载 SPI。
 * <p>用户可通过注册自定义 {@link RemoteMessageLoader} bean 接入 Nacos/Apollo 等任意配置中心，
 * 未注册时使用内置 {@link HttpRemoteMessageLoader}（HTTP 拉取 properties）。
 */
public interface RemoteMessageLoader {
    
    /**
     * 加载全部语言的文案。
     *
     * @return locale → key → 文案
     */
    Map<Locale, Properties> load();
    
}
