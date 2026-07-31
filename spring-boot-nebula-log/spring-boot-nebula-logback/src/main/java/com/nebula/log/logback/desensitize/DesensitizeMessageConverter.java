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

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback message converter that desensitizes {@code %msg} output.
 *
 * <p>Register via:
 * <pre>{@code
 * <conversionRule conversionWord="msg"
 *     converterClass="com.nebula.log.logback.desensitize.DesensitizeMessageConverter"/>
 * }</pre>
 *
 * <p>Rules are controlled by {@code nebula.log.desensitize.*} through {@link DesensitizeRuntime}.
 */
public class DesensitizeMessageConverter extends MessageConverter {
    
    @Override
    public String convert(ILoggingEvent event) {
        return DesensitizeRuntime.apply(super.convert(event));
    }
}
