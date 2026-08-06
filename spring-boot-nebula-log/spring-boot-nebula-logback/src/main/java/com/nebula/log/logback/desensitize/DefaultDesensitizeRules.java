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

import com.nebula.log.logback.desensitize.rule.BankCardDesensitizeRule;
import com.nebula.log.logback.desensitize.rule.EmailDesensitizeRule;
import com.nebula.log.logback.desensitize.rule.IdCardDesensitizeRule;
import com.nebula.log.logback.desensitize.rule.MobileDesensitizeRule;
import com.nebula.log.logback.desensitize.rule.SecretKeyDesensitizeRule;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Built-in rules plus {@link ServiceLoader} custom rules.
 */
public final class DefaultDesensitizeRules {
    
    private DefaultDesensitizeRules() {
    }
    
    /**
     * Built-ins (secretKey → email → idCard → mobile → bankCard) plus SPI rules.
     */
    public static List<DesensitizeRule> all() {
        List<DesensitizeRule> rules = new ArrayList<>();
        rules.add(new SecretKeyDesensitizeRule());
        rules.add(new EmailDesensitizeRule());
        rules.add(new IdCardDesensitizeRule());
        rules.add(new MobileDesensitizeRule());
        rules.add(new BankCardDesensitizeRule());
        for (DesensitizeRule rule : ServiceLoader.load(DesensitizeRule.class)) {
            rules.add(rule);
        }
        return List.copyOf(rules);
    }
}
