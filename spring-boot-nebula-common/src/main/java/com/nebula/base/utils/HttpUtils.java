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
 
package com.nebula.base.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

/**
 * @author : wh
 * @date : 2025/3/19 14:24
 * @description:
 */
@Slf4j
public class HttpUtils {
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private static final MediaType MEDIA_TYPE_FILE = MediaType.parse("application/octet-stream");
    
    private static volatile OkHttpClient okHttpClient;
    
    private static final long DEFAULT_TIMEOUT = 30;
    
    private static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            synchronized (HttpUtils.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return okHttpClient;
    }
    
    public static String get(String url) throws IOException {
        return get(url, null);
    }
    
    public static String get(String url, Map<String, String> headers) {
        try {
            Request request = buildGetRequest(url, headers);
            try (Response response = getInstance().newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("GET请求失败 url={}, headers={}", url, headers, e);
            throw new HttpException("HTTP请求失败", e);
        }
    }
    
    public static void getAsync(String url, Callback callback) {
        getAsync(url, null, callback);
    }
    
    public static void getAsync(String url, Map<String, String> headers, Callback callback) {
        Request request = buildGetRequest(url, headers);
        getInstance().newCall(request).enqueue(callback);
    }
    
    // 同步POST表单（优化异常处理）
    public static String postFormSync(String url, Map<String, String> params) {
        return postFormSync(url, null, params);
    }
    
    public static String postFormSync(String url, Map<String, String> headers,
                                      Map<String, String> params) {
        try {
            Request request = buildFormPostRequest(url, headers, params);
            try (Response response = getInstance().newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("POST表单失败 url={}, headers={}, params={}", url, headers, params, e);
            throw new HttpException("HTTP请求失败", e);
        }
    }
    
    public static void postFormAsync(String url, Map<String, String> params, Callback callback) {
        postFormAsync(url, null, params, callback);
    }
    
    public static void postFormAsync(String url, Map<String, String> headers,
                                     Map<String, String> params, Callback callback) {
        Request request = buildFormPostRequest(url, headers, params);
        getInstance().newCall(request).enqueue(callback);
    }
    
    /**
     * POST JSON sync
     *
     * @param url
     * @param json
     * @return
     */
    public static String postJson(String url, String json) {
        return postJson(url, null, json);
    }
    
    public static String postJson(String url, Map<String, String> headers,
                                  String json) {
        try {
            
            Request request = buildJsonPostRequest(url, headers, json);
            try (Response response = getInstance().newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (IOException e) {
            log.error("POST JSON失败 url={}, headers={}, json={}", url, headers, json, e);
            throw new HttpException("HTTP请求失败", e);
        }
    }
    
    // 异步POST JSON
    public static void postJsonAsync(String url, String json, Callback callback) {
        postJsonAsync(url, null, json, callback);
    }
    
    public static void postJsonAsync(String url, Map<String, String> headers,
                                     String json, Callback callback) {
        Request request = buildJsonPostRequest(url, headers, json);
        getInstance().newCall(request).enqueue(callback);
    }
    
    // 文件上传
    public static String uploadFileSync(String url, File file) throws IOException {
        return uploadFileSync(url, null, file);
    }
    
    public static String uploadFileSync(String url, Map<String, String> headers,
                                        File file) throws IOException {
        Request request = buildFileUploadRequest(url, headers, file);
        try (Response response = getInstance().newCall(request).execute()) {
            return handleResponse(response);
        }
    }
    
    // 构建GET请求
    private static Request buildGetRequest(String url, Map<String, String> headers) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null)
            throw new IllegalArgumentException("Invalid URL");
        
        Request.Builder builder = new Request.Builder()
                .url(httpUrl)
                .get();
        
        addHeaders(builder, headers);
        return builder.build();
    }
    
    // 构建表单POST请求
    private static Request buildFormPostRequest(String url, Map<String, String> headers,
                                                Map<String, String> params) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(formBuilder.build());
        
        addHeaders(builder, headers);
        return builder.build();
    }
    
    // 构建JSON POST请求
    private static Request buildJsonPostRequest(String url, Map<String, String> headers,
                                                String json) {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        
        addHeaders(builder, headers);
        return builder.build();
    }
    
    // 构建文件上传请求
    private static Request buildFileUploadRequest(String url, Map<String, String> headers,
                                                  File file) {
        RequestBody body = RequestBody.create(file, MEDIA_TYPE_FILE);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        
        addHeaders(builder, headers);
        return builder.build();
    }
    
    // 添加请求头
    private static void addHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }
    
    // 处理响应
    private static String handleResponse(@NotNull Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("HTTP状态码异常: " + response.code());
        }
        
        try (ResponseBody body = response.body()) {
            return body != null ? body.string() : null;
        }
    }
    
    public static class HttpException extends RuntimeException {
        
        public HttpException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
}
