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
 
package com.nebula.alert.feishu;

import com.nebula.base.utils.HttpUtils;
import com.nebula.base.utils.StringUtils;
import com.nebula.base.utils.juc.ThreadFactoryImpl;
import com.nebula.base.utils.ThreadFactoryImpl;
import com.nebula.web.common.utils.NebulaSysWebUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : wh
 * @date : 2025/3/19 14:16
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class FeiShuRoot {
    
    private final NebulaSysWebUtils nebulaSysWebUtils;
    
    private final ExecutorService threadPoolTaskExecutor;
    
    public FeiShuRoot(NebulaSysWebUtils nebulaSysWebUtils) {
        this.nebulaSysWebUtils = nebulaSysWebUtils;
        this.threadPoolTaskExecutor = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), new ThreadFactoryImpl("feishu-"));
    }
    
    private final static String FEISHU_CAR = "{\n" +
            "    \"msg_type\": \"interactive\",\n" +
            "    \"card\":%s\n" +
            "}";
    
    /**
     * send  rich text by feishu robot
     *
     * @param url
     * @param richText
     * @param args
     */
    public void sendRichText(String url, String richText, Object... args) {
        String msg = String.format(richText, args);
        if (log.isDebugEnabled()) {
            log.debug("发送webhook {} {}", url, msg);
        }
        String post = HttpUtils.postJson(url, String.format(FEISHU_CAR, msg));
        if (log.isDebugEnabled()) {
            log.debug("webhook返回 {}", post);
        }
    }
    
    public void sendRichTextAsync(String url, String richText, Object... args) {
        threadPoolTaskExecutor.submit(() -> {
            try {
                sendRichText(url, richText, args);
            } catch (Exception e) {
                log.error("Failed to send async message to FeiShu: {}", e.getMessage(), e);
            }
        });
    }
    
    /**
     * 发送飞书文本消息
     *
     * @param str text消息
     * @param url 机器人robot地址
     */
    public String sendText(String str, String url) {
        str = StringUtils.stringFormat("【{}】{}", nebulaSysWebUtils.getActive(), str);
        String jsonStr = String.format("{\"msg_type\": \"text\",\"content\": {\"text\": \"%s\"}}", str);
        
        return HttpUtils.postJson(url, jsonStr);
    }
    
}
