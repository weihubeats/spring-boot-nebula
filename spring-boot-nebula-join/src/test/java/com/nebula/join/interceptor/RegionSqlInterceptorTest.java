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
 
package com.nebula.join.interceptor;

import com.nebula.join.context.RouteContextConfig;
import com.nebula.join.exception.NoRegionException;
import com.nebula.join.properties.RegionRouteProperties;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionSqlInterceptorTest {
    
    private final RegionRouteProperties properties = new RegionRouteProperties();
    
    private String invokeRewrite(String sql) throws Exception {
        Method rewrite = RegionSqlInterceptor.class.getDeclaredMethod("rewriteSql",
                String.class, List.class, RouteContextConfig.class);
        rewrite.setAccessible(true);
        RouteContextConfig config = new RouteContextConfig(true,
                properties.getMainColumn(), properties.getJoinTable(), properties.getJoinColumn());
        return (String) rewrite.invoke(new RegionSqlInterceptor(properties), sql, List.of(1L, 2L), config);
    }
    
    @Test
    void rewritesSimpleSelectWithRegionFilter() throws Exception {
        String rewritten = invokeRewrite("SELECT id, uid FROM user_info WHERE uid = 1");
        
        assertTrue(rewritten.contains("JOIN"), rewritten);
        assertTrue(rewritten.contains("IN"), rewritten);
        assertTrue(rewritten.toLowerCase().contains("csa_user_route"), rewritten);
    }
    
    @Test
    @DisplayName("FROM 子查询无法安全改写时必须快速失败（回归：此前静默放行未过滤的 SQL）")
    void subQueryFromFailsFast() throws Exception {
        String subQuerySql = "SELECT * FROM (SELECT uid FROM user_info) t";
        
        // 反射调用抛出的异常会被包装为 InvocationTargetException，这里直接断言原因类型
        Exception wrapper = assertThrows(Exception.class, () -> invokeRewrite(subQuerySql));
        assertTrue(wrapper.getCause() instanceof NoRegionException,
                "expected NoRegionException but got: " + wrapper.getCause());
    }
    
    @Test
    void subQueryFromSkippedWhenFailFastDisabled() throws Exception {
        properties.setFailOnUnrewritable(false);
        String subQuerySql = "SELECT * FROM (SELECT uid FROM user_info) t";
        
        assertEquals(subQuerySql, invokeRewrite(subQuerySql));
    }
    
    @Test
    void nonSelectStatementPassesThrough() throws Exception {
        // rewriteSql 只做读隔离，非 SELECT 语句原样返回
        assertEquals("INSERT INTO t VALUES (1)", invokeRewrite("INSERT INTO t VALUES (1)"));
    }
}
