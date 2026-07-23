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
 
package com.nebula.feign.log;

import com.nebula.feign.config.NebulaFeignProperties;
import feign.Capability;
import feign.Client;
import java.util.Objects;

/**
 * 通过 Capability 将 {@link NebulaFeignLogFilter} 挂到 Feign Client 链上。
 */
public class NebulaFeignLogCapability implements Capability {
    
    private final NebulaFeignProperties properties;
    
    public NebulaFeignLogCapability(NebulaFeignProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }
    
    @Override
    public Client enrich(Client client) {
        return new NebulaFeignLogFilter(client, properties);
    }
}
