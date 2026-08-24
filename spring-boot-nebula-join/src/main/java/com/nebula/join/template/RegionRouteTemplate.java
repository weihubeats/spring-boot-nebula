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
 
package com.nebula.join.template;

import com.nebula.join.context.RegionRouteHelper;

import java.util.function.Supplier;

public class RegionRouteTemplate {
    
    /**
     * 执行带返回值的业务逻辑，并开启区域路由改写
     *
     * @param supplier 业务逻辑
     * @param <T>      返回值类型
     * @return 业务执行结果
     */
    public <T> T execute(Supplier<T> supplier) {
        RegionRouteHelper.startScope();
        try {
            return supplier.get();
        } finally {
            RegionRouteHelper.endScope();
        }
    }
    
    /**
     * 执行无返回值的业务逻辑，并开启区域路由改写
     *
     * @param runnable 业务逻辑
     */
    public void run(Runnable runnable) {
        RegionRouteHelper.startScope();
        try {
            runnable.run();
        } finally {
            RegionRouteHelper.endScope();
        }
    }
}