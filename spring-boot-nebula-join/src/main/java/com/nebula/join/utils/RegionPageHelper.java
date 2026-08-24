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
 
package com.nebula.join.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nebula.join.context.RegionRouteHelper;

public class RegionPageHelper {
    
    /**
     * 分页并开启区域路由改写。
     * scope 计数不再由 SQL 拦截器递减：Web 场景下由 RegionWebInterceptor.afterCompletion 统一 clear()，
     * 非 Web 场景请用 RegionRouteTemplate 包裹业务并在 finally 中释放。
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页 Page
     * @param <E> 元素类型
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize) {
        RegionRouteHelper.startScope();
        return PageHelper.startPage(pageNum, pageSize);
    }
}