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
 
package com.nebula.distribute.lock.sample.controller;

import com.nebula.distribute.lock.sample.dto.OrderDTO;
import com.nebula.distribute.lock.sample.service.TestService;
import com.nebula.web.boot.annotation.NebulaResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wh
 * @date : 2023/8/2 10:28
 * @description:
 */
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class TestController {
    
    private final TestService testServcie;
    
    @GetMapping("/test")
    @NebulaResponseBody
    public String test(OrderDTO dto) {
        testServcie.updateOrder(dto);
        return "小奏";
    }
    
}
