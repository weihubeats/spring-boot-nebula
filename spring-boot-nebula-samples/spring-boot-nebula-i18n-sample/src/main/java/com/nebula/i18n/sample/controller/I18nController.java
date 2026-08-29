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
 
package com.nebula.i18n.sample.controller;

import com.nebula.i18n.core.NebulaI18nMessage;
import com.nebula.i18n.sample.enums.SampleErrorCode;
import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.enums.ResultCode;
import com.nebula.web.boot.exception.BizException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 国际化使用示例：
 * <ul>
 *   <li>错误码消息按请求语言自动翻译（ResultCode.messageKey）</li>
 *   <li>业务代码通过 {@link NebulaI18nMessage} 取当前语言文案</li>
 * </ul>
 * 语言来源优先级：X-Lang header → lang query → Accept-Language → 默认语言。
 */
@RestController
@RequestMapping("/i18n")
public class I18nController {
    
    @GetMapping("/error")
    public NebulaResponse<Void> error() {
        return NebulaResponse.fail(ResultCode.PARAM_MISS);
    }
    
    @GetMapping("/unauthorized")
    public NebulaResponse<Void> unauthorized() {
        return NebulaResponse.fail(ResultCode.UNAUTHORIZED);
    }
    
    @GetMapping("/not-found")
    public NebulaResponse<Void> notFound() {
        return NebulaResponse.fail(ResultCode.NOT_FOUND);
    }
    
    @GetMapping("/biz-error")
    public NebulaResponse<Void> bizError() {
        throw new BizException(ResultCode.FAILURE.getMessage());
    }
    
    @GetMapping("/order-not-found")
    public NebulaResponse<Void> orderNotFound() {
        return NebulaResponse.fail(SampleErrorCode.ORDER_NOT_FOUND);
    }
    
    @GetMapping("/stock-not-enough")
    public NebulaResponse<Void> stockNotEnough() {
        return NebulaResponse.fail(SampleErrorCode.STOCK_NOT_ENOUGH);
    }
    
    @GetMapping("/remote-only")
    public NebulaResponse<String> remoteOnly() {
        return NebulaResponse.data(NebulaI18nMessage.get("remote.only"), null);
    }
    
    @GetMapping("/greeting")
    public NebulaResponse<String> greeting(@RequestParam(defaultValue = "nebula") String name) {
        return NebulaResponse.data(NebulaI18nMessage.get("greeting", name), null);
    }
    
}
