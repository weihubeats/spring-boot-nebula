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

import com.nebula.log.logback.desensitize.RegexDesensitizeRule;
import java.util.regex.Pattern;

/**
 * Mainland China mobile number: {@code 138****8000}.
 */
public final class MobileDesensitizeRule extends RegexDesensitizeRule {
    
    public static final String NAME = "mobile";
    
    private static final Pattern PATTERN = Pattern.compile("(?<!\\d)(1[3-9]\\d)\\d{4}(\\d{4})(?!\\d)");
    
    public MobileDesensitizeRule() {
        super(NAME, PATTERN, m -> m.group(1) + "****" + m.group(2));
    }
}
