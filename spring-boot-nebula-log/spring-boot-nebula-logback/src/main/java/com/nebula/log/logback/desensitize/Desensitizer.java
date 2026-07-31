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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Applies a chain of {@link DesensitizeRule}s to log message text.
 */
public final class Desensitizer {
    
    private final List<DesensitizeRule> rules;
    
    public Desensitizer(List<? extends DesensitizeRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }
    
    public static Desensitizer defaults() {
        return new Desensitizer(DefaultDesensitizeRules.all());
    }
    
    public static Desensitizer of(Collection<? extends DesensitizeRule> rules, Collection<String> disabledNames) {
        List<DesensitizeRule> source = Objects.isNull(rules) || rules.isEmpty()
                ? DefaultDesensitizeRules.all()
                : List.copyOf(rules);
        if (Objects.isNull(disabledNames) || disabledNames.isEmpty()) {
            return new Desensitizer(source);
        }
        Set<String> disabled = disabledNames.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        List<DesensitizeRule> filtered = source.stream()
                .filter(rule -> !disabled.contains(rule.name().toLowerCase(Locale.ROOT)))
                .toList();
        return new Desensitizer(filtered);
    }
    
    public static Desensitizer defaultsExcluding(Collection<String> disabledNames) {
        return of(DefaultDesensitizeRules.all(), disabledNames);
    }
    
    public Desensitizer withExtra(DesensitizeRule... extra) {
        List<DesensitizeRule> merged = new ArrayList<>(rules);
        if (Objects.nonNull(extra)) {
            Stream.of(extra).filter(Objects::nonNull).forEach(merged::add);
        }
        return new Desensitizer(merged);
    }
    
    public String apply(String input) {
        if (Objects.isNull(input) || input.isEmpty() || rules.isEmpty()) {
            return input;
        }
        String result = input;
        for (DesensitizeRule rule : rules) {
            result = rule.apply(result);
        }
        return result;
    }
    
    public List<DesensitizeRule> rules() {
        return rules;
    }
}
