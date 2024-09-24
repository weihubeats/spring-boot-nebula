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
 
package com.nebula.web.common.utils;

import java.util.Objects;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : wh
 * @date : 2024/3/21 10:21
 * @description:
 */
@ConfigurationProperties(prefix = "nebula")
@Data
public class NebulaSysWebUtils {
    
    private static final String DEV = "dev";
    
    private static final String TEST = "test";
    
    private static final String STAGE = "stage";
    
    private static final String PRD = "prd";


    private static final String SRE = "dd";
    /**
     * 开发环境
     */
    @Value("${spring.profiles.active:dev}")
    private String active;
    
    /**
     * 服务名
     */
    @Value("${spring.application.name:unknown}")
    private String applicationName;
    
    public boolean isDev() {
        return Objects.equals(active, DEV);
    }
    
    public boolean isTest() {
        return Objects.equals(active, TEST);
    }
    
    public boolean isPrd() {
        return Objects.equals(active, PRD);
    }
    
    public boolean isStage() {
        return Objects.equals(active, STAGE);
    }
    
}
