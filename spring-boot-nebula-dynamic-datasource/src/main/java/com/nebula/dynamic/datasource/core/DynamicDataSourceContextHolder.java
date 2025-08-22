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
 
package com.nebula.dynamic.datasource.core;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
public class DynamicDataSourceContextHolder {
    
    private static final ThreadLocal<Deque<String>> CONTEXT_HOLDER = ThreadLocal.withInitial(LinkedList::new);
    
    /**
     * 设置数据源
     */
    public static void setDataSource(String dataSource) {
        CONTEXT_HOLDER.get().push(dataSource);
    }
    
    /**
     * 获取当前数据源
     */
    public static String getDataSource() {
        Deque<String> deque = CONTEXT_HOLDER.get();
        return deque.isEmpty() ? null : deque.peek();
    }
    
    /**
     * 清除当前数据源（回退到上一个，如果有的话）
     */
    public static void clear() {
        Deque<String> deque = CONTEXT_HOLDER.get();
        if (!deque.isEmpty()) {
            deque.pop();
        }
        if (deque.isEmpty()) {
            CONTEXT_HOLDER.remove();
        }
    }
    
}
