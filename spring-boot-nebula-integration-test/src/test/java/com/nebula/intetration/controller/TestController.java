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
 
package com.nebula.intetration.controller;

import com.nebula.base.utils.JsonUtil;
import com.nebula.integration.IntegrationApplication;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author : wh
 * @date : 2025/3/24
 * @description:
 */
@SpringBootTest(classes = IntegrationApplication.class, properties = "spring.profiles.active=prd")
@ActiveProfiles("prd")
@AutoConfigureMockMvc
public class TestController {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testSendFeishuRobot() throws Exception {
        String jsonBody = "{\"msg_type\":\"text\",\"content\":{\"text\":\"hello\"}}";
        mockMvc.perform(MockMvcRequestBuilders.post("/error")
                .contentType(MediaType.APPLICATION_JSON)
                .param("uid", "99")
                .content(Objects.requireNonNull(JsonUtil.toJSONString(jsonBody))))
                .andExpect(status().is5xxServerError()).andDo(print());
        
    }
    
    @Nested
    @SpringBootTest(classes = IntegrationApplication.class, properties = {"spring.profiles.active=prd", "nebula.web.monitor.type=feishu"})
    @ActiveProfiles("prd")
    @AutoConfigureMockMvc
    class TestBB {
        
        @Autowired
        private MockMvc mockMvc;
        
        @Test
        public void testSendFeishuRobot() throws Exception {
            String jsonBody = "{\"msg_type\":\"text\",\"content\":{\"text\":\"hello\"}}";
            mockMvc.perform(MockMvcRequestBuilders.post("/error")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("uid", "99")
                    .content(Objects.requireNonNull(JsonUtil.toJSONString(jsonBody))))
                    .andExpect(status().is5xxServerError()).andDo(print());
            
            TimeUnit.SECONDS.sleep(2);
            
        }
    }
    
}
