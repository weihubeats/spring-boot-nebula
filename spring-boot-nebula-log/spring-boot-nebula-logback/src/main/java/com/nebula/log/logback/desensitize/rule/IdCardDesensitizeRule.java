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
 
package com.nebula.log.logback.desensitize.rule;

import com.nebula.log.logback.desensitize.DesensitizeRule;
import com.nebula.log.logback.desensitize.RegexDesensitizeRule;
import java.util.regex.Pattern;

/**
 * 15/18-digit Chinese ID card; keeps first 4 and last 4.
 */
public final class IdCardDesensitizeRule implements DesensitizeRule {
    
    public static final String NAME = "idCard";
    
    private static final Pattern ID_CARD_18 =
            Pattern.compile("(?<!\\d)(\\d{4})\\d{10}(\\d{3}[0-9Xx])(?!\\d)");
    private static final Pattern ID_CARD_15 = Pattern.compile("(?<!\\d)(\\d{4})\\d{7}(\\d{4})(?!\\d)");
    
    private final RegexDesensitizeRule id18 =
            new RegexDesensitizeRule(NAME, ID_CARD_18, m -> m.group(1) + "**********" + m.group(2));
    private final RegexDesensitizeRule id15 =
            new RegexDesensitizeRule(NAME, ID_CARD_15, m -> m.group(1) + "*******" + m.group(2));
    
    @Override
    public String name() {
        return NAME;
    }
    
    @Override
    public String apply(String input) {
        return id15.apply(id18.apply(input));
    }
}
