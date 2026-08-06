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
 
package com.nebula.log.sample.controller;

import com.nebula.web.boot.annotation.NebulaResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo endpoints for log desensitization and ERROR Feishu alert.
 */
@Slf4j
@RestController
@RequestMapping("/log")
public class LogDemoController {
    
    /**
     * Logs sensitive fields; console/file output should be masked via DesensitizeMessageConverter.
     */
    @GetMapping("/desensitize")
    @NebulaResponseBody
    public String desensitize() {
        log.info(
                "user mobile=13812348000 email=alice@example.com password=secret123 card=6222021234567890123");
        return "logged sensitive payload, check console for masked output";
    }
    
    /**
     * Triggers log.error; FeishuErrorAppender is registered from nebula.log.feishu.* when enabled.
     */
    @GetMapping("/error")
    @NebulaResponseBody
    public String error() {
        try {
            throw new IllegalStateException("demo error for feishu alert");
        } catch (IllegalStateException e) {
            log.error("order failed, mobile=13812348000", e);
        }
        return "error logged, check feishu webhook if configured";
    }
}
