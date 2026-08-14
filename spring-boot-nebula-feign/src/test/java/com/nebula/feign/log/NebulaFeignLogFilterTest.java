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
 
package com.nebula.feign.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nebula.feign.config.NebulaFeignProperties;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.Target;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.LogLevel;

/**
 * NebulaFeignLogFilter 单元测试。
 */
class NebulaFeignLogFilterTest {
    
    private NebulaFeignProperties properties;
    
    @BeforeEach
    void setUp() {
        properties = new NebulaFeignProperties();
        properties.getLog().setLevel(LogLevel.INFO);
        properties.getLog().getSlow().setEnabled(true);
        properties.getLog().getSlow().setThresholdMillis(50L);
        properties.getLog().getSlow().setLevel(LogLevel.ERROR);
    }
    
    @Test
    void shouldDelegateAndPreserveResponseBody() throws IOException {
        Client delegate = mock(Client.class);
        Request request = Request.create(Request.HttpMethod.POST, "http://localhost/users?id=1",
                Collections.emptyMap(), "{\"name\":\"小奏\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
        Response origin = Response.builder()
                .status(200)
                .reason("OK")
                .request(request)
                .headers(Collections.emptyMap())
                .body("{\"ok\":true}", StandardCharsets.UTF_8)
                .build();
        when(delegate.execute(any(Request.class), any(Request.Options.class))).thenReturn(origin);
        
        NebulaFeignLogFilter filter = new NebulaFeignLogFilter(delegate, properties);
        Response response = filter.execute(request, new Request.Options());
        
        assertEquals(200, response.status());
        assertEquals("{\"ok\":true}", new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8));
        verify(delegate).execute(any(Request.class), any(Request.Options.class));
    }
    
    @Test
    void shouldRethrowWhenDelegateFails() throws IOException {
        Client delegate = mock(Client.class);
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost/users",
                Collections.emptyMap(), null, StandardCharsets.UTF_8);
        when(delegate.execute(any(Request.class), any(Request.Options.class)))
                .thenThrow(new IOException("connection reset"));
        
        NebulaFeignLogFilter filter = new NebulaFeignLogFilter(delegate, properties);
        
        assertThrows(IOException.class, () -> filter.execute(request, new Request.Options()));
    }
    
    @Test
    void shouldTriggerSlowCallWhenExceedThreshold() throws Exception {
        Client delegate = mock(Client.class);
        Request request = Request.create(Request.HttpMethod.POST, "http://localhost/users",
                Collections.emptyMap(), "{\"name\":\"slow\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
        Response origin = Response.builder()
                .status(200)
                .reason("OK")
                .request(request)
                .headers(Collections.emptyMap())
                .body("{\"ok\":true}", StandardCharsets.UTF_8)
                .build();
        when(delegate.execute(any(Request.class), any(Request.Options.class))).thenAnswer(invocation -> {
            Thread.sleep(80);
            return origin;
        });
        
        NebulaFeignLogFilter filter = new NebulaFeignLogFilter(delegate, properties);
        Response response = filter.execute(request, new Request.Options());
        
        assertEquals(200, response.status());
    }
    
    @Test
    void shouldResolveClientNameFromRequestTemplate() {
        Target<Object> target = new Target.HardCodedTarget<>(Object.class, "userClient", "http://localhost");
        RequestTemplate template = new RequestTemplate()
                .feignTarget(target)
                .method(Request.HttpMethod.GET);
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost/users",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, template);
        
        assertEquals("userClient", NebulaFeignLogFilter.clientName(request));
    }
    
    @Test
    void shouldFallbackToUnknownWithoutTemplate() {
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost/users",
                Collections.emptyMap(), null, StandardCharsets.UTF_8);
        
        assertEquals("unknown", NebulaFeignLogFilter.clientName(request));
    }
    
    @Test
    void shouldFormatRequestMultilineWithClientName() {
        Target<Object> target = new Target.HardCodedTarget<>(Object.class, "userClient", "http://localhost");
        RequestTemplate template = new RequestTemplate()
                .feignTarget(target)
                .method(Request.HttpMethod.POST);
        Request request = Request.create(Request.HttpMethod.POST, "http://localhost/users",
                Collections.emptyMap(), "{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8, template);
        NebulaFeignLogFilter filter = new NebulaFeignLogFilter(mock(Client.class), properties);
        
        String log = filter.formatRequest(request, 12L, "{\"name\":\"x\"}", 200, "{\"ok\":true}");
        
        assertEquals("Feign [userClient] POST http://localhost/users cost=12ms\n"
                + "requestBody={\"name\":\"x\"}\n"
                + "responseStatus=200\n"
                + "responseBody={\"ok\":true}", log);
    }
    
    @Test
    void shouldTruncateOverlongBody() {
        properties.getLog().setMaxBodyLength(16);
        NebulaFeignLogFilter filter = new NebulaFeignLogFilter(mock(Client.class), properties);
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost/users",
                Collections.emptyMap(), null, StandardCharsets.UTF_8);
        
        String log = filter.formatRequest(request, 1L, "0123456789abcdefghij", 200, "ok");
        
        assertEquals("Feign [unknown] GET http://localhost/users cost=1ms\n"
                + "requestBody=0123456789abcdef...(truncated)\n"
                + "responseStatus=200\n"
                + "responseBody=ok", log);
    }
    
    @Test
    void shouldRejectNegativeMaxBodyLength() {
        assertThrows(IllegalArgumentException.class,
                () -> properties.getLog().setMaxBodyLength(-1));
    }
    
    @Test
    void shouldAcceptZeroMaxBodyLength() {
        properties.getLog().setMaxBodyLength(0);
        assertEquals(0, properties.getLog().getMaxBodyLength());
    }
}
