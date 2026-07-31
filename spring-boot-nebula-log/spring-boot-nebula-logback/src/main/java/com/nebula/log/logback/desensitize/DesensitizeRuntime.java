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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Runtime desensitize settings shared between Logback converters and Spring configuration.
 *
 * <p>Logback initializes before the Spring context; converters read this holder on each
 * {@code convert}, while {@code NebulaLogProperties} updates it after binding.
 */
public final class DesensitizeRuntime {
    
    private static volatile boolean enabled = true;
    private static volatile Desensitizer desensitizer = Desensitizer.defaults();
    
    private DesensitizeRuntime() {
    }
    
    public static void configure(boolean enabled, Collection<String> disableRules) {
        configure(enabled, disableRules, List.of());
    }
    
    /**
     * @param extraRules custom rules (e.g. Spring beans), appended after built-ins / SPI
     */
    public static void configure(
                                 boolean enabled, Collection<String> disableRules, Collection<? extends DesensitizeRule> extraRules) {
        DesensitizeRuntime.enabled = enabled;
        if (!enabled) {
            DesensitizeRuntime.desensitizer = new Desensitizer(Collections.emptyList());
            return;
        }
        List<DesensitizeRule> merged = new ArrayList<>(DefaultDesensitizeRules.all());
        if (Objects.nonNull(extraRules)) {
            for (DesensitizeRule rule : extraRules) {
                if (Objects.nonNull(rule)) {
                    merged.add(rule);
                }
            }
        }
        DesensitizeRuntime.desensitizer = Desensitizer.of(
                merged, Objects.requireNonNullElse(disableRules, Collections.emptyList()));
    }
    
    public static String apply(String message) {
        if (!enabled) {
            return message;
        }
        return desensitizer.apply(message);
    }
    
    public static boolean enabled() {
        return enabled;
    }
    
    public static Desensitizer desensitizer() {
        return desensitizer;
    }
    
    /**
     * Restores defaults; for tests only.
     */
    public static void reset() {
        enabled = true;
        desensitizer = Desensitizer.defaults();
    }
}
