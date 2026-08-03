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

import com.nebula.feign.sample.client.UserClient;
import com.nebula.feign.sample.dto.UserRequest;
import com.nebula.feign.sample.dto.UserVO;
import com.nebula.web.boot.annotation.NebulaResponseBody;
import com.nebula.web.boot.api.NebulaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消费端：通过 Feign 调用本机 provider，验证自动解包与日志。
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {
    
    private final UserClient userClient;
    
    public ConsumerController(UserClient userClient) {
        this.userClient = userClient;
    }
    
    @GetMapping("/users/{id}")
    @NebulaResponseBody
    public UserVO getUser(@PathVariable Long id) {
        return userClient.getUser(id);
    }
    
    @GetMapping("/users/{id}/raw")
    @NebulaResponseBody
    public NebulaResponse<UserVO> getUserRaw(@PathVariable Long id) {
        return userClient.getUserRaw(id);
    }
    
    @GetMapping("/users/{id}/rpc-error")
    @NebulaResponseBody
    public UserVO getUserRpcError(@PathVariable Long id) {
        return userClient.getUserRpcError(id);
    }
    
    @PostMapping("/users")
    @NebulaResponseBody
    public UserVO createUser(@RequestBody UserRequest request) {
        return userClient.createUser(request);
    }
    
    @PostMapping("/users/{id}")
    @NebulaResponseBody
    public UserVO updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return userClient.updateUser(id, request);
    }
    
    @PostMapping("/users/search")
    @NebulaResponseBody
    public UserVO searchUser(@RequestBody UserRequest request) {
        return userClient.searchUser(request);
    }
    
    @PostMapping("/users/slow")
    @NebulaResponseBody
    public UserVO slowCreate(@RequestBody UserRequest request) {
        return userClient.slowCreate(request);
    }
}
