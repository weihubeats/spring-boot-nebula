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
 
package com.nebula.i18n.sample.remote;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * 模拟远程文案配置服务（独立端口 8092）。
 * <p>返回 properties 文件内容，文案与本地资源刻意不同，用于演示远程优先覆盖本地。
 * 通过 JDK 内置 {@link HttpServer} 启动，无额外依赖。
 */
@Slf4j
public final class MockRemoteMessageServer {
    
    private static final int PORT = 8092;
    
    private static final Pattern LOCALE_PATTERN = Pattern.compile("_([a-zA-Z_]+)\\.properties$");
    
    private static final Map<String, String> CONTENT = Map.of(
            "zh_CN",
            "greeting=\u8fdc\u7a0b\u4f60\u597d\uff0c{0}\uff01\n"
                    + "remote.only=\u8fdc\u7a0b\u72ec\u6709\u6587\u6848\n",
            "en_US",
            "greeting=[remote] Hello, {0}!\n"
                    + "remote.only=Remote only message\n");
    
    private MockRemoteMessageServer() {
    }
    
    /**
     * 启动 mock 远程服务，须在 Spring 应用启动前调用（应用初始化加载远程文案依赖它）。
     */
    public static void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/remote", exchange -> {
                String locale = extractLocale(exchange.getRequestURI().getPath());
                byte[] body = CONTENT.getOrDefault(locale, "").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            });
            server.setExecutor(Executors.newFixedThreadPool(2, new DaemonThreadFactory()));
            server.start();
            log.info("[i18n-sample] mock remote message server started on port {}", PORT);
        } catch (IOException e) {
            log.warn("[i18n-sample] mock remote message server start failed", e);
        }
    }
    
    private static String extractLocale(String path) {
        Matcher matcher = LOCALE_PATTERN.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    private static final class DaemonThreadFactory implements ThreadFactory {
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "mock-remote-msg");
            thread.setDaemon(true);
            return thread;
        }
    }
    
}
