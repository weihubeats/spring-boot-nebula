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
 
package com.nebula.join.sample;

import com.nebula.join.context.RegionRouteHelper;
import com.nebula.join.context.RouteContextConfig;
import com.nebula.join.sample.entity.MerchantDO;
import com.nebula.join.sample.entity.OrderDO;
import com.nebula.join.sample.entity.UserDO;
import com.nebula.join.sample.mapper.MerchantMapper;
import com.nebula.join.sample.mapper.OrderMapper;
import com.nebula.join.sample.mapper.UserMapper;
import com.nebula.join.utils.RegionPageHelper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest
public class RegionInterceptorTest {
    
    @SpringBootApplication
    @MapperScan("com.nebula.join.sample.mapper")
    @ComponentScan("com.nebula.join")
    static class TestConfig {
    }
    
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    
    @BeforeEach
    public void setup() {
        // 模拟当前登录用户所在的 Region 为 1
        RegionRouteHelper.setRegions(Collections.singletonList(1L));
        // 注意：这里我们不设置 ManualEnabled，完全依赖注解触发
    }
    
    @AfterEach
    public void cleanup() {
        RegionRouteHelper.endScope();
    }
    
    @Test
    public void testDefaultRoute_User() {
        System.out.println("----- 测试默认配置 (User) -----");
        // 预期 SQL: ... INNER JOIN csa_user_route cur ON t1.uid = cur.uid WHERE ... cur.csa_user_region IN (1)
        List<UserDO> users = userMapper.selectAllUsers();
        
        // u001 在 region 1, u002 在 region 2, u003 无记录
        // 应该只查出 u001
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals("u001", users.get(0).getUid());
    }
    
    @Test
    public void testDefaultRoute_UserAndPageHelper() {
        System.out.println("----- 测试默认配置 (User) + PageHelper-----");
        
        RegionPageHelper.startPage(1, 10);
        // PageHelper.startPage(1, 10);
        
        // 预期 SQL: ... INNER JOIN csa_user_route cur ON t1.uid = cur.uid WHERE ... cur.csa_user_region IN (1)
        List<UserDO> users = userMapper.selectAllUsers();
        
        // u001 在 region 1, u002 在 region 2, u003 无记录
        // 应该只查出 u001
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals("u001", users.get(0).getUid());
        
        List<UserDO> users2 = userMapper.selectAllUsersNoRegion();
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals("u001", users.get(0).getUid());
        
    }
    
    @Test
    public void testCustomMainColumn_Order() {
        System.out.println("----- 测试自定义主表字段 (Order) -----");
        // 预期 SQL: ... ON t1.creating_uid = cur.uid ...
        List<OrderDO> orders = orderMapper.selectAllOrders();
        
        // Order 1 (u001) -> region 1 -> 可见
        // Order 2 (u003) -> 无路由 -> 不可见
        Assertions.assertEquals(1, orders.size());
        Assertions.assertEquals("ORD_001", orders.get(0).getOrderNo());
    }
    
    @Test
    public void testFullCustom_Merchant() {
        System.out.println("----- 测试自定义路由表和字段 (Merchant) -----");
        // 预期 SQL: ... JOIN csa_merchant_route cur ON t1.merchant_code = cur.m_id ...
        List<MerchantDO> merchants = merchantMapper.selectAllMerchants();
        
        // m001 -> region 1 -> 可见
        // m002 -> 无路由 -> 不可见
        Assertions.assertEquals(1, merchants.size());
        Assertions.assertEquals("m001", merchants.get(0).getMerchantCode());
    }
    
    @Test
    public void testDynamicContext_Order() {
        System.out.println("----- 测试 Context 动态配置 (模拟 PageHelper/动态表名) -----");
        
        // 1. 准备动态配置
        // 假设我们要查询 t_order，但是方法上没有注解
        // 我们通过代码告诉拦截器：主表用 creating_uid，路由表用 csa_user_route，路由字段用 uid
        RouteContextConfig config = RouteContextConfig.builder()
                .mainColumn("creating_uid") // 动态指定主表关联字段
                .joinTable("csa_user_route") // 动态指定路由表名 (也可以是 csa_user_route_2024)
                .joinColumn("uid") // 动态指定路由表字段
                .build();
        
        try {
            RegionRouteHelper.setRegions(Collections.singletonList(1L));
            RegionRouteHelper.startScope(config);
            
            List<OrderDO> orders = orderMapper.selectOrdersDynamic();
            Assertions.assertEquals(1, orders.size());
            Assertions.assertEquals("ORD_001", orders.get(0).getOrderNo());
            System.out.println("动态配置生效：成功拦截并过滤了数据");
            
        } finally {
            RegionRouteHelper.endScope();
        }
    }
}