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
 
package com.nebula.web.boot.monitor;

import com.nebula.alert.feishu.FeiShuRoot;
import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 飞书告警渠道。
 * <p>负责模板渲染、UTF-8 字节截断与飞书机器人推送。
 */
@Slf4j
public class FeishuAlertChannel implements NebulaAlertChannel {
    
    private final FeiShuRoot feiShuRoot;
    
    private final String webhookUrl;
    
    private final String template;
    
    private static final int FEISHU_MESSAGE_HASH_MAX_LENGTH = 15 * 1024;
    
    private static final String TEMPLATE_PATH = "config/feishu.json";
    
    public FeishuAlertChannel(FeiShuRoot feiShuRoot, String webhookUrl) {
        this.feiShuRoot = feiShuRoot;
        this.webhookUrl = webhookUrl;
        try {
            this.template = readUtf8String(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new IllegalStateException("飞书告警模板加载失败: " + TEMPLATE_PATH, e);
        }
    }
    
    @Override
    public void send(AlertMessage message) {
        String stack = truncateByUtf8Bytes(stackTraceToJsonValue(message.cause()),
                FEISHU_MESSAGE_HASH_MAX_LENGTH);
        String body = truncateByUtf8Bytes(message.body(), FEISHU_MESSAGE_HASH_MAX_LENGTH);
        if (body.length() > 2) {
            body = body.substring(1, body.length() - 2);
        }
        String params = message.parameters();
        if (DataUtils.isNotEmpty(params)) {
            params = params.replace("\"", "\\\"");
        }
        feiShuRoot.sendRichTextAsync(webhookUrl, template, params, body, stack, message.uri());
    }
    
    private static String readUtf8String(String path) throws IOException {
        try (InputStream inputStream = FeishuAlertChannel.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    private static String truncateByUtf8Bytes(String value, int maxBytes) {
        return Utf8TextUtils.truncateByUtf8Bytes(value, maxBytes);
    }
    
    private static String stackTraceToJsonValue(Throwable ex) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            
            // 使用 Jackson 处理转义
            return JsonUtil.getInstance().writeValueAsString(stackTrace).replace("\"", "");
        } catch (Exception e) {
            return "Error formatting stack trace: " + e.getMessage();
        }
    }
}
