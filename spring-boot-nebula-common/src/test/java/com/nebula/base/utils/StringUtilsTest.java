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
 
package com.nebula.base.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StringUtilsTest {
    
    // ---- isInteger ----
    
    @Test
    void isIntegerDigits() {
        assertTrue(StringUtils.isInteger("123"));
        assertTrue(StringUtils.isInteger("0"));
    }
    
    @Test
    void isIntegerEmptyStringMatches() {
        // 正则 ^[0-9]*$ 允许空串，保持既有行为
        assertTrue(StringUtils.isInteger(""));
    }
    
    @Test
    void isIntegerNonDigits() {
        assertFalse(StringUtils.isInteger("12a3"));
        assertFalse(StringUtils.isInteger("-123"));
        assertFalse(StringUtils.isInteger("12.3"));
        assertFalse(StringUtils.isInteger(" 123"));
    }
    
    // ---- trimAnySpace ----
    
    @Test
    void trimAnySpaceRemovesAllWhitespace() {
        assertEquals("abc", StringUtils.trimAnySpace(" a b\tc\n"));
        assertEquals("abc", StringUtils.trimAnySpace("a b c"));
    }
    
    @Test
    void trimAnySpacePlainStringUnchanged() {
        assertEquals("abc", StringUtils.trimAnySpace("abc"));
        assertEquals("", StringUtils.trimAnySpace("   "));
    }
    
    // ---- firstUpperCase / firstLowerCase ----
    
    @Test
    void firstUpperCaseLowerLetter() {
        assertEquals("Abc", StringUtils.firstUpperCase("abc"));
    }
    
    @Test
    void firstUpperCaseNonLetterUnchanged() {
        // 旧实现 cs[0] -= 32 会把 '1' 变成 '/'，修复后应保持不变
        assertEquals("1abc", StringUtils.firstUpperCase("1abc"));
        assertEquals("_foo", StringUtils.firstUpperCase("_foo"));
        assertEquals("中文", StringUtils.firstUpperCase("中文"));
    }
    
    @Test
    void firstUpperCaseAlreadyUpper() {
        assertEquals("Abc", StringUtils.firstUpperCase("Abc"));
    }
    
    @Test
    void firstLowerCaseUpperLetter() {
        assertEquals("abc", StringUtils.firstLowerCase("Abc"));
    }
    
    @Test
    void firstLowerCaseNonLetterUnchanged() {
        assertEquals("1abc", StringUtils.firstLowerCase("1abc"));
        assertEquals("_Foo", StringUtils.firstLowerCase("_Foo"));
    }
    
    // ---- emoji encode/decode 正常路径 + 异常路径保留 cause ----
    
    @Test
    void emojiEncodeDecodeRoundTrip() {
        String s = "hi😀";
        String encoded = StringUtils.emojiEncode(s);
        assertEquals("hi[[EMOJI:%F0%9F%98%80]]", encoded);
        assertEquals(s, StringUtils.emojiDecode(encoded));
    }
    
    @Test
    void emojiEncodeErrorPathKeepsCause() {
        // null 会使 RegexUtils 抛 NPE，外层 catch 后应带 cause 抛出
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> StringUtils.emojiEncode(null));
        assertNotNullCause(ex);
    }
    
    @Test
    void emojiDecodeErrorPathKeepsCause() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> StringUtils.emojiDecode(null));
        assertNotNullCause(ex);
    }
    
    private static void assertNotNullCause(RuntimeException ex) {
        assertTrue(ex.getCause() != null, "exception should keep original cause");
    }
}
