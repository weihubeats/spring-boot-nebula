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
 
package com.nebula.web.boot.api;

import com.nebula.web.boot.annotation.NebulaMessageKey;
import java.io.Serializable;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description: 内部错误码契约，code 统一为 int；对外 JSON 形态由协议层配置决定
 */
public interface IErrorCode extends Serializable {
    
    int getCode();
    
    String getMessage();
    
    /**
     * 国际化消息 key，可选。返回非空时以该值为准（手动指定）；
     * 未指定（返回 null）时自动生成，规则见 {@link #resolveMessageKey()}。
     *
     * @return 显式指定的消息 key；默认 null（自动生成）
     */
    default String getMessageKey() {
        return null;
    }
    
    /**
     * 最终生效的消息 key，优先级：显式 {@link #getMessageKey()} → {@link NebulaMessageKey} 注解 → 自动生成。
     * <p>自动生成规则（仅枚举实现）：{@code 蛇形类名.蛇形常量名}，如
     * {@code ResultCode.PARAM_MISS → result_code.param_miss}。
     * 非枚举实现且未显式指定时返回 null（不参与国际化，直接使用 {@link #getMessage()}）。
     *
     * @return 消息 key；null 表示无国际化
     */
    default String resolveMessageKey() {
        String explicit = getMessageKey();
        if (explicit != null && !explicit.isBlank()) {
            return explicit;
        }
        if (this instanceof Enum<?> enumValue) {
            try {
                NebulaMessageKey annotation =
                        enumValue.getClass().getField(enumValue.name()).getAnnotation(NebulaMessageKey.class);
                if (annotation != null && !annotation.value().isBlank()) {
                    return annotation.value();
                }
            } catch (NoSuchFieldException ignored) {
                // 枚举常量必然存在对应字段，忽略
            }
            String clazz = toSnakeCase(getClass().getSimpleName());
            String constant = toSnakeCase(enumValue.name());
            if (clazz != null && constant != null) {
                return clazz + "." + constant;
            }
        }
        return null;
    }
    
    /**
     * 驼峰/大写常量转小写蛇形：ResultCode → result_code，PARAM_MISS → param_miss，HTTPError → http_error。
     */
    static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(input.length() + 4);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                boolean boundary = sb.length() > 0
                        && (Character.isLowerCase(input.charAt(i - 1))
                                || (i + 1 < input.length() && Character.isLowerCase(input.charAt(i + 1))));
                if (boundary) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else if (c == '_') {
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
}
