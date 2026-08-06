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
    
    private static final class State {
        
        private final boolean enabled;
        private final Desensitizer desensitizer;
        
        State(boolean enabled, Desensitizer desensitizer) {
            this.enabled = enabled;
            this.desensitizer = desensitizer;
        }
    }
    
    private static volatile State state = new State(true, Desensitizer.defaults());
    
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
        List<DesensitizeRule> merged;
        if (!enabled) {
            merged = Collections.emptyList();
        } else {
            merged = new ArrayList<>(DefaultDesensitizeRules.all());
            if (Objects.nonNull(extraRules)) {
                for (DesensitizeRule rule : extraRules) {
                    if (Objects.nonNull(rule)) {
                        merged.add(rule);
                    }
                }
            }
        }
        Desensitizer newDesensitizer = Desensitizer.of(
                merged, Objects.requireNonNullElse(disableRules, Collections.emptyList()));
        state = new State(enabled, newDesensitizer);
    }
    
    public static String apply(String message) {
        State s = state;
        if (!s.enabled) {
            return message;
        }
        return s.desensitizer.apply(message);
    }
    
    public static boolean enabled() {
        return state.enabled;
    }
    
    public static Desensitizer desensitizer() {
        return state.desensitizer;
    }
    
    /**
     * Restores defaults; for tests only.
     */
    public static void reset() {
        state = new State(true, Desensitizer.defaults());
    }
}
