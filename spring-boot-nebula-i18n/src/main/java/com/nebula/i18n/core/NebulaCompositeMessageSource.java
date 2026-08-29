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
 
package com.nebula.i18n.core;

import java.text.MessageFormat;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.AbstractMessageSource;

/**
 * 组合 MessageSource：远程文案优先，未命中下探本地资源兜底。
 * <p>替代 Spring 6 已移除的 {@code CompositeMessageSource}。
 */
public class NebulaCompositeMessageSource extends AbstractMessageSource {
    
    private final MessageSource remote;
    
    private final MessageSource local;
    
    public NebulaCompositeMessageSource(MessageSource remote, MessageSource local) {
        this.remote = remote;
        this.local = local;
    }
    
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        MessageFormat format = resolveFrom(remote, code, locale);
        if (format == null) {
            format = resolveFrom(local, code, locale);
        }
        return format;
    }
    
    private MessageFormat resolveFrom(MessageSource source, String code, Locale locale) {
        if (source == null) {
            return null;
        }
        try {
            String message = source.getMessage(code, null, null, locale);
            if (message != null) {
                return createMessageFormat(message, locale);
            }
        } catch (NoSuchMessageException ignored) {
            // 未命中，继续下探
        }
        return null;
    }
    
}
