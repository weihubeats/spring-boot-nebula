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
 
package com.nebula.web.boot.monitor;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * UTF-8 文本截断工具（告警消息长度限制共用逻辑）。
 */
final class Utf8TextUtils {
    
    private Utf8TextUtils() {
    }
    
    /**
     * 按字节截断字符串，避免在多字节 UTF-8 字符中间截断产生乱码。
     */
    static String truncateByUtf8Bytes(String value, int maxBytes) {
        if (Objects.isNull(value)) {
            return "";
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }
        int end = findUtf8Boundary(bytes, 0, maxBytes);
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }
    
    /**
     * 在 [off, len) 范围内向前寻找不拆散多字节 UTF-8 字符的安全截断边界。
     */
    static int findUtf8Boundary(byte[] buf, int off, int len) {
        int end = off;
        for (int i = len - 1; i >= off; i--) {
            int b = buf[i] & 0xFF;
            if (b < 0x80) {
                end = i + 1;
                break;
            }
            if ((b & 0xC0) == 0x80) {
                end = i + 1;
            } else {
                break;
            }
        }
        return end;
    }
}
