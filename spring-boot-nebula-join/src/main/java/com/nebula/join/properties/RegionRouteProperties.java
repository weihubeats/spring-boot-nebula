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
 
package com.nebula.join.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "region-route")
@Data
public class RegionRouteProperties {
    
    private boolean enabled = true;
    
    private String joinTable = "csa_user_route";
    
    private String regionColumnName = "csa_region_id";
    
    private String joinColumn = "uid";
    
    private String mainColumn = "uid";
    
    private String headerName = "X-REGION";
    
    /**
     * 遇到无法安全改写的 SQL（如 FROM 子查询）时是否快速失败。
     * 关闭后此类 SQL 将跳过区域过滤直接执行（存在越权风险，不建议关闭）。
     */
    private boolean failOnUnrewritable = true;
}
