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

import static org.assertj.core.api.Assertions.assertThat;

import com.nebula.web.common.utils.SpringBeanUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 无 Spring 上下文时 {@link NebulaResponse} 保持原有默认 int 逻辑。
 */
class NebulaResponseNoContextTest {
    
    @BeforeAll
    static void setUp() {
        new SpringBeanUtils().setApplicationContext(null);
        NebulaResponse.resetCodeResolverForTest();
    }
    
    @Test
    void keepsIntCodeWithoutContext() {
        assertThat(NebulaResponse.toWireCode(200)).isEqualTo(200);
        assertThat(NebulaResponse.toWireCode(500)).isEqualTo(500);
        assertThat(NebulaResponse.matchesCode(200, 200)).isTrue();
        assertThat(NebulaResponse.matchesCode(200, "Success")).isFalse();
        
        NebulaResponse<String> response = NebulaResponse.data("ok", "success");
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(NebulaResponse.isSuccess(response)).isTrue();
    }
}
