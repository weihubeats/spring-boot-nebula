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
 
package com.nebula.log.logback.desensitize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DesensitizerTest {
    
    private final Desensitizer desensitizer = Desensitizer.defaults();
    
    @Test
    void masksMobile() {
        assertEquals("phone=138****8000", desensitizer.apply("phone=13812348000"));
    }
    
    @Test
    void masksIdCard18() {
        String input = "id=110101199001011234";
        String masked = desensitizer.apply(input);
        assertTrue(masked.startsWith("id=1101"));
        assertTrue(masked.endsWith("1234"));
        assertTrue(masked.contains("**********"));
        assertFalse(masked.contains("19900101"));
    }
    
    @Test
    void masksEmail() {
        assertEquals("a***@example.com", desensitizer.apply("alice@example.com"));
    }
    
    @Test
    void masksSecretKey() {
        assertEquals("password=***", desensitizer.apply("password=secret123"));
        assertEquals("token: ***", desensitizer.apply("token: abc-xyz"));
        assertEquals("accessKey=\"***\"", desensitizer.apply("accessKey=\"AKIA123\""));
    }
    
    @Test
    void masksSecretKeyInJson() {
        assertEquals("{\"password\":\"***\"}", desensitizer.apply("{\"password\":\"secret123\"}"));
        assertEquals(
                "request {\"mobile\":\"138****8000\",\"password\":\"***\",\"email\":\"a***@example.com\"}",
                desensitizer.apply(
                        "request {\"mobile\":\"13812348000\",\"password\":\"secret123\",\"email\":\"alice@example.com\"}"));
        assertEquals("{\"token\":\"***\",\"secret\":\"***\"}",
                desensitizer.apply("{\"token\":\"eyJhbGciOiJIUzI1NiJ9\",\"secret\":\"abc123\"}"));
    }
    
    @Test
    void masksBankCard() {
        String masked = desensitizer.apply("card=6222021234567890123");
        assertTrue(masked.contains("****0123"));
        assertFalse(masked.contains("622202123456789"));
    }
    
    @Test
    void leavesNormalTextUntouched() {
        assertEquals("hello world 12345", desensitizer.apply("hello world 12345"));
    }
    
    @Test
    void disableRulesSkipsSelected() {
        Desensitizer withoutEmail = Desensitizer.defaultsExcluding(List.of("email", "bankCard"));
        assertEquals("alice@example.com", withoutEmail.apply("alice@example.com"));
        assertEquals("138****8000", withoutEmail.apply("13812348000"));
    }
    
    @Test
    void appliesMultipleRulesInOneMessage() {
        String input = "user=13812348000 mail=bob@test.com password=pwd1";
        String masked = desensitizer.apply(input);
        assertTrue(masked.contains("138****8000"));
        assertTrue(masked.contains("b***@test.com"));
        assertTrue(masked.contains("password=***"));
    }
}
