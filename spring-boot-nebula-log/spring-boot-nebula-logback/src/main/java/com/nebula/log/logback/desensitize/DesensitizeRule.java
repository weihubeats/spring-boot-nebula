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

/**
 * SPI for log message desensitization rules.
 *
 * <p>Built-in rules live under {@code com.nebula.log.logback.desensitize.rule}.
 * Custom rules can be added by:
 * <ul>
 *   <li>registering a Spring {@code @Bean} / {@code @Component} of this type, or</li>
 *   <li>Java {@link java.util.ServiceLoader} entry in {@code META-INF/services/}</li>
 * </ul>
 */
public interface DesensitizeRule {
    
    /**
     * Stable rule id used by {@code nebula.log.desensitize.disable-rules}.
     */
    String name();
    
    /**
     * Masks sensitive fragments in {@code input}; return {@code input} when nothing matches.
     */
    String apply(String input);
}
