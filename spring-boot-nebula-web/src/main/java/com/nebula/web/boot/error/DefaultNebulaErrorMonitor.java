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
 
package com.nebula.web.boot.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebula.alert.feishu.FeiShuRoot;
import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.JsonUtil;
import com.nebula.web.boot.config.NebulaWebProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StreamUtils;

/**
 * @author : wh
 * @date : 2025/3/25
 * @description:
 */
@RequiredArgsConstructor
public class DefaultNebulaErrorMonitor implements NebulaErrorMonitor {
    
    // todo 先写死
    private final FeiShuRoot feiShuRoot;
    
    private final NebulaWebProperties nebulaWebProperties;
    
    // todo test
    private final String text = "{\n" +
            "    \"schema\": \"2.0\",\n" +
            "    \"config\": {\n" +
            "        \"update_multi\": true,\n" +
            "        \"style\": {\n" +
            "            \"text_size\": {\n" +
            "                \"normal_v2\": {\n" +
            "                    \"default\": \"normal\",\n" +
            "                    \"pc\": \"normal\",\n" +
            "                    \"mobile\": \"heading\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"direction\": \"vertical\",\n" +
            "        \"padding\": \"12px 12px 12px 12px\",\n" +
            "        \"elements\": [\n" +
            "            {\n" +
            "                \"tag\": \"div\",\n" +
            "                \"text\": {\n" +
            "                    \"tag\": \"plain_text\",\n" +
            "                    \"content\": \"param: %s\",\n" +
            "                    \"text_size\": \"normal_v2\",\n" +
            "                    \"text_align\": \"left\",\n" +
            "                    \"text_color\": \"default\"\n" +
            "                },\n" +
            "                \"margin\": \"0px 0px 0px 0px\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"tag\": \"hr\",\n" +
            "                \"margin\": \"0px 0px 0px 0px\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"tag\": \"div\",\n" +
            "                \"text\": {\n" +
            "                    \"tag\": \"plain_text\",\n" +
            "                    \"content\": \"body: %s\",\n" +
            "                    \"text_size\": \"normal_v2\",\n" +
            "                    \"text_align\": \"left\",\n" +
            "                    \"text_color\": \"default\"\n" +
            "                },\n" +
            "                \"margin\": \"0px 0px 0px 0px\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"tag\": \"hr\",\n" +
            "                \"margin\": \"0px 0px 0px 0px\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"tag\": \"div\",\n" +
            "                \"text\": {\n" +
            "                    \"tag\": \"plain_text\",\n" +
            "                    \"content\": \"异常: %s\",\n" +
            "                    \"text_size\": \"normal_v2\",\n" +
            "                    \"text_align\": \"left\",\n" +
            "                    \"text_color\": \"default\"\n" +
            "                },\n" +
            "                \"margin\": \"0px 0px 0px 0px\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"header\": {\n" +
            "        \"title\": {\n" +
            "            \"tag\": \"plain_text\",\n" +
            "            \"content\": \"接口 %s\"\n" +
            "        },\n" +
            "        \"subtitle\": {\n" +
            "            \"tag\": \"plain_text\",\n" +
            "            \"content\": \"\"\n" +
            "        },\n" +
            "        \"template\": \"blue\",\n" +
            "        \"padding\": \"12px 12px 12px 12px\"\n" +
            "    }\n" +
            "}";
    
    private static final int FEISHU_MESSAGE_HASH_MAX_LENGTH = 15 * 1024;
    
    @Override
    public void monitorError(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            sendFeiShuErrorMsg(request, response, handler, ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    /**
     * 发送飞书错误信息
     */
    public void sendFeiShuErrorMsg(HttpServletRequest request, HttpServletResponse response, Object handler,
                                   Exception ex) throws IOException {
        String uri = request.getRequestURI();
        String body = new String(StreamUtils.copyToByteArray(request.getInputStream()), request.getCharacterEncoding());
        
        // 删掉多余的转义字符
        String errorStackMsg = stackTraceToJsonValue(ex);
        if (errorStackMsg.getBytes(StandardCharsets.UTF_8).length > FEISHU_MESSAGE_HASH_MAX_LENGTH) {
            errorStackMsg = errorStackMsg.substring(0, new String(new byte[FEISHU_MESSAGE_HASH_MAX_LENGTH]).length());
        }
        if (body.getBytes(StandardCharsets.UTF_8).length > FEISHU_MESSAGE_HASH_MAX_LENGTH) {
            body = body.substring(0, new String(new byte[FEISHU_MESSAGE_HASH_MAX_LENGTH]).length());
        }
        
        body = body.substring(1, body.length() - 2);
        String jsonString = JsonUtil.toJSONString(request.getParameterMap());
        if (DataUtils.isNotEmpty(jsonString)) {
            jsonString = jsonString.replace("\"", "\\\"");
        }
        feiShuRoot.sendRichTextAsync(nebulaWebProperties.getMonitorUrl(), text, jsonString, body, errorStackMsg, uri);
    }
    
    public static String stackTraceToJsonValue(Throwable ex) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            
            // 使用 Jackson 处理转义
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(stackTrace).replace("\"", "");
        } catch (Exception e) {
            return "Error formatting stack trace: " + e.getMessage();
        }
    }
    
}
