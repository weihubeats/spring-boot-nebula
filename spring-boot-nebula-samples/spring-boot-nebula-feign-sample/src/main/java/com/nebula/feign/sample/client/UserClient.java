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
 
package com.nebula.feign.sample.client;

import com.nebula.feign.sample.dto.UserRequest;
import com.nebula.feign.sample.dto.UserVO;
import com.nebula.web.boot.api.NebulaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign 客户端：业务类型会由 NebulaFeignDecoder 自动从 NebulaResponse 解包。
 */
@FeignClient(name = "userClient", url = "${feign.client.user.url}")
public interface UserClient {
    
    @GetMapping("/provider/users/{id}")
    UserVO getUser(@PathVariable("id") Long id);
    
    @GetMapping("/provider/users/{id}")
    NebulaResponse<UserVO> getUserRaw(@PathVariable("id") Long id);
    
    @GetMapping("/provider/users/{id}/rpc-error")
    UserVO getUserRpcError(@PathVariable("id") Long id);
    
    @PostMapping("/provider/users")
    UserVO createUser(@RequestBody UserRequest request);
    
    @PostMapping("/provider/users/{id}")
    UserVO updateUser(@PathVariable("id") Long id, @RequestBody UserRequest request);
    
    @PostMapping("/provider/users/search")
    UserVO searchUser(@RequestBody UserRequest request);
    
    @PostMapping("/provider/users/slow")
    UserVO slowCreate(@RequestBody UserRequest request);
}
