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
 
package com.nebula.join.template;

import com.nebula.join.context.RegionRouteHelper;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionRouteTemplateTest {
    
    @AfterEach
    void cleanUp() {
        RegionRouteHelper.clear();
    }
    
    @Test
    void executeClearsScopeAfterSuccess() {
        RegionRouteTemplate template = new RegionRouteTemplate();
        
        String result = template.execute(() -> {
            assertTrue(RegionRouteHelper.isRewriteEnabled());
            return "ok";
        });
        
        assertEquals("ok", result);
        assertFalse(RegionRouteHelper.isRewriteEnabled());
    }
    
    @Test
    @DisplayName("execute 内业务抛异常时 scope 必须被清理（回归：此前无 finally 导致 ThreadLocal 泄漏）")
    void executeClearsScopeOnException() {
        RegionRouteTemplate template = new RegionRouteTemplate();
        
        assertThrows(IllegalStateException.class, () -> template.execute(() -> {
            throw new IllegalStateException("boom");
        }));
        
        assertFalse(RegionRouteHelper.isRewriteEnabled());
    }
    
    @Test
    void nestedScopesOnlyReleaseOnce() {
        RegionRouteTemplate template = new RegionRouteTemplate();
        AtomicInteger innerCalls = new AtomicInteger();
        
        template.execute(() -> {
            template.run(innerCalls::incrementAndGet);
            // 内层 scope 结束后外层仍然生效
            assertTrue(RegionRouteHelper.isRewriteEnabled());
            return null;
        });
        
        assertEquals(1, innerCalls.get());
        assertFalse(RegionRouteHelper.isRewriteEnabled());
    }
    
    @Test
    void runKeepsConfigWithinScope() {
        RegionRouteHelper.setRegions(List.of(1L, 2L));
        try {
            new RegionRouteTemplate().run(() -> assertEquals(List.of(1L, 2L), RegionRouteHelper.getRegions()));
        } finally {
            RegionRouteHelper.clear();
        }
    }
}
