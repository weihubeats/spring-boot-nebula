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
 
package com.nebula.log.logback.feishu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nebula.base.utils.HttpUtils;
import com.nebula.base.utils.JsonUtil;
import java.io.InputStream;
import java.util.Objects;

/**
 * Feishu bot webhook client using {@link HttpUtils} (OkHttp) and {@link JsonUtil}.
 */
public class FeishuWebhookClient {
    
    private final JsonNode cardTemplate;
    
    public FeishuWebhookClient() {
        this.cardTemplate = loadCardTemplate();
    }
    
    FeishuWebhookClient(JsonNode cardTemplate) {
        this.cardTemplate = Objects.requireNonNull(cardTemplate, "cardTemplate");
    }
    
    /**
     * Posts a Feishu interactive card. {@code cardJson} is the card body without the outer envelope.
     */
    public void sendRichText(String webhookUrl, String cardJson) {
        ObjectNode envelope = JsonUtil.getInstance().createObjectNode();
        envelope.put("msg_type", "interactive");
        envelope.set("card", JsonUtil.json2JsonNode(cardJson));
        HttpUtils.postJson(webhookUrl, JsonUtil.jsonNodeToString(envelope));
    }
    
    /**
     * Fills the card template with logger / message / stack / header fields.
     */
    public String buildCard(String loggerName, String message, String stack, String header) {
        ObjectNode card = cardTemplate.deepCopy();
        putElementContent(card, 0, "logger: " + nullToEmpty(loggerName));
        putElementContent(card, 2, "message: " + nullToEmpty(message));
        putElementContent(card, 4, "异常: " + nullToEmpty(stack));
        ObjectNode titleNode = (ObjectNode) card.path("header").path("title");
        titleNode.put("content", nullToEmpty(header));
        return JsonUtil.jsonNodeToString(card);
    }
    
    private static void putElementContent(ObjectNode card, int elementIndex, String content) {
        ObjectNode text = (ObjectNode) card.path("body").path("elements").get(elementIndex).path("text");
        text.put("content", content);
    }
    
    private static String nullToEmpty(String value) {
        return Objects.isNull(value) ? "" : value;
    }
    
    static JsonNode loadCardTemplate() {
        String path = "config/log-feishu.json";
        try (InputStream in = FeishuWebhookClient.class.getClassLoader().getResourceAsStream(path)) {
            if (Objects.isNull(in)) {
                throw new IllegalStateException("Missing classpath resource: " + path);
            }
            JsonNode node = JsonUtil.json2JsonNode(in);
            if (Objects.isNull(node) || node.isMissingNode()) {
                throw new IllegalStateException("Invalid card template: " + path);
            }
            return node;
        } catch (Exception e) {
            if (e instanceof IllegalStateException ise) {
                throw ise;
            }
            throw new IllegalStateException("Failed to load " + path, e);
        }
    }
}
