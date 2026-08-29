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
 
package com.nebula.i18n.sample.enums;

import com.nebula.web.boot.annotation.NebulaMessageKey;
import com.nebula.web.boot.api.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 自定义错误码：演示两种国际化 key 指定方式。
 * <ul>
 *   <li>{@link NebulaMessageKey} 注解手动指定 key（order.not_found）</li>
 *   <li>未标注自动生成（sample_error_code.stock_not_enough）</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum SampleErrorCode implements IErrorCode {
    
    @NebulaMessageKey("order.not_found")
    ORDER_NOT_FOUND(40001, "订单不存在"),
    
    STOCK_NOT_ENOUGH(40002, "库存不足");
    
    private final int code;
    
    private final String message;
    
}
