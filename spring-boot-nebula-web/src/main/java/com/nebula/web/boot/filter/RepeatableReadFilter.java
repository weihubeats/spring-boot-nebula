package com.nebula.web.boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RepeatableReadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String contentType = request.getContentType();

        // 1. 如果是文件上传类型，直接放行，千万不要去包装
        if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 包装 Request
        RepeatableReadRequestWrapper requestWrapper = null;
        try {
            requestWrapper = new RepeatableReadRequestWrapper(request);
        } catch (Exception e) {
            log.error("Nebula SDK: 包装请求体失败", e);
        }

        // 3. 将包装后的 request 往下传递（如果没有包装成功则传递原对象）
        if (requestWrapper == null) {
            filterChain.doFilter(request, response);
        } else {
            filterChain.doFilter(requestWrapper, response);
        }
    }
}
