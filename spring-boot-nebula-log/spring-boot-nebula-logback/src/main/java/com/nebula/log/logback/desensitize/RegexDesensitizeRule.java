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

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-based {@link DesensitizeRule}. Prefer this when adding custom pattern rules.
 */
public class RegexDesensitizeRule implements DesensitizeRule {
    
    private final String name;
    private final Pattern pattern;
    private final Function<Matcher, String> replacer;
    
    public RegexDesensitizeRule(String name, Pattern pattern, Function<Matcher, String> replacer) {
        this.name = Objects.requireNonNull(name, "name");
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.replacer = Objects.requireNonNull(replacer, "replacer");
    }
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public String apply(String input) {
        if (Objects.isNull(input) || input.isEmpty()) {
            return input;
        }
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        do {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacer.apply(matcher)));
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }
}
