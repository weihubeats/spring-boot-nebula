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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DesensitizeMessageConverterTest {
    
    @AfterEach
    void reset() {
        DesensitizeRuntime.reset();
    }
    
    @Test
    void convertsAndMasksMessage() {
        DesensitizeMessageConverter converter = new DesensitizeMessageConverter();
        converter.start();
        
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test");
        LoggingEvent event =
                new LoggingEvent("fqcn", logger, Level.INFO, "mobile={}", null, new Object[]{"13812348000"});
        
        assertEquals("mobile=138****8000", converter.convert(event));
    }
    
    @Test
    void respectsRuntimeDisableRules() {
        DesensitizeRuntime.configure(true, List.of("mobile", "email"));
        DesensitizeMessageConverter converter = new DesensitizeMessageConverter();
        converter.start();
        
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test");
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "13812348000 alice@x.com", null, null);
        
        assertEquals("13812348000 alice@x.com", converter.convert(event));
    }
    
    @Test
    void respectsRuntimeDisabled() {
        DesensitizeRuntime.configure(false, List.of());
        DesensitizeMessageConverter converter = new DesensitizeMessageConverter();
        converter.start();
        
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test");
        LoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "13812348000", null, null);
        
        assertEquals("13812348000", converter.convert(event));
    }
}
