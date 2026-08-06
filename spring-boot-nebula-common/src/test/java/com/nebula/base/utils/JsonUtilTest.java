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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * JsonUtil 基础行为校验。
 */
class JsonUtilTest {
    
    @Test
    void shouldSerializeAndDeserialize() {
        Map<String, Object> source = Map.of("name", "小奏", "age", 18);
        String json = JsonUtil.toJson(source);
        Map<?, ?> restored = JsonUtil.fromJson(json, Map.class);
        
        assertEquals("小奏", restored.get("name"));
        assertEquals(18, restored.get("age"));
    }
    
    @Test
    void shouldReturnNullWhenClassDeserializeFails() {
        assertNull(JsonUtil.fromJson("not-json", Map.class));
        assertNull(JsonUtil.fromJson("not-json".getBytes(StandardCharsets.UTF_8), Map.class));
        assertNull(JsonUtil.fromJson((String) null, Map.class));
        assertNull(JsonUtil.fromJson(new byte[0], Map.class));
    }
    
    @Test
    void shouldParseJsonNodeFromBytes() {
        String json = "{\"ok\":true}";
        assertTrue(JsonUtil.fromJsonNode(json.getBytes(StandardCharsets.UTF_8)).get("ok").asBoolean());
        assertTrue(JsonUtil.fromJsonNode(json).get("ok").asBoolean());
    }
}
