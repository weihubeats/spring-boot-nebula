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
 
package com.nebula.join.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AutoJoin {
    
    /**
    * 主表的关联字段 (Main Table Join Column)
    * 例如：业务表中的 user_id, creating_uid
    * 默认值：uid
    */
    String mainColumn() default "uid";
    
    /**
     * 需要join的表名
     * 如果为空，则使用全局配置(RegionRouteProperties)中的默认表名
     */
    String joinTable() default "csa_user_route";
    
    String joinColumn() default "uid";
    
}
