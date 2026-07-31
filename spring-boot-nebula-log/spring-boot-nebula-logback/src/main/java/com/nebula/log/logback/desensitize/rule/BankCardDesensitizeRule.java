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
 * Bank card-like digit sequences (16–19); keeps last 4.
 */
public final class BankCardDesensitizeRule extends RegexDesensitizeRule {
    
    public static final String NAME = "bankCard";
    
    private static final Pattern PATTERN = Pattern.compile("(?<!\\d)(\\d{12,15})(\\d{4})(?!\\d)");
    
    public BankCardDesensitizeRule() {
        super(NAME, PATTERN, m -> "****" + m.group(2));
    }
}
