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
 
package com.nebula.feign.sample.controller;

import com.nebula.feign.sample.dto.UserRequest;
import com.nebula.feign.sample.dto.UserVO;
import com.nebula.web.boot.annotation.NebulaResponseBody;
import com.nebula.web.boot.exception.BizException;
import com.nebula.web.boot.exception.RpcException;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟下游服务：通过 @NebulaResponseBody 包装为统一响应。
 */
@RestController
@RequestMapping("/provider")
public class ProviderController {
    
    private final AtomicLong idGenerator = new AtomicLong(100);
    
    @GetMapping("/users/{id}")
    @NebulaResponseBody
    public UserVO getUser(@PathVariable Long id) {
        if (id < 0) {
            throw new BizException("用户不存在");
        }
        return new UserVO(id, "小奏-" + id);
    }
    
    /**
     * HTTP 500 + NebulaResponse，用于验证 ErrorDecoder 解析异常码。
     */
    @GetMapping("/users/{id}/rpc-error")
    @NebulaResponseBody
    public UserVO rpcError(@PathVariable Long id) {
        throw new RpcException("下游 RPC 失败: " + id);
    }
    
    @PostMapping("/users")
    @NebulaResponseBody
    public UserVO createUser(@RequestBody UserRequest request) {
        return new UserVO(idGenerator.incrementAndGet(), request.name());
    }
    
    @PostMapping("/users/{id}")
    @NebulaResponseBody
    public UserVO updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        if (id < 0) {
            throw new BizException("用户不存在");
        }
        return new UserVO(id, request.name());
    }
    
    @PostMapping("/users/search")
    @NebulaResponseBody
    public UserVO searchUser(@RequestBody UserRequest request) {
        return new UserVO(1L, request.name());
    }
    
    /**
     * 模拟慢接口，用于验证慢调用告警。
     */
    @PostMapping("/users/slow")
    @NebulaResponseBody
    public UserVO slowCreate(@RequestBody UserRequest request) throws InterruptedException {
        Thread.sleep(1200);
        return new UserVO(idGenerator.incrementAndGet(), request.name());
    }
}
