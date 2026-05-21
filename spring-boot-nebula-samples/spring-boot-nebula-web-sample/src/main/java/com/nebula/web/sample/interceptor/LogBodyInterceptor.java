package com.nebula.web.sample.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class LogBodyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 在请求到达 Controller 之前，强行读取 Body
        String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        System.out.println("==== 【拦截器日志】请求进来了，Body是: " + body + " ====");
        return true; // 放行
    }
}
