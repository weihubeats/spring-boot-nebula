package com.nebula.web.boot.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] bodyCache;
    // 标记当前请求是否被缓存。如果是不支持缓存的类型（如文件上传），则设为 false
    private final boolean isCacheable;

    public RepeatableReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        String contentType = request.getContentType();
        // 跳过大文件/表单上传类型的缓存，防止 OOM
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            this.isCacheable = false;
            this.bodyCache = new byte[0];
        } else {
            this.isCacheable = true;
            // 只有普通的文本/JSON请求才将其完整读取到内存
            this.bodyCache = StreamUtils.copyToByteArray(request.getInputStream());
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 如果不支持缓存，直接返回原生的流，避免破坏文件上传等功能
        if (!isCacheable) {
            return super.getInputStream();
        }

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bodyCache);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 空实现
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // 如果不支持缓存，返回原生 Reader
        if (!isCacheable) {
            return super.getReader();
        }

        // 处理可能为空的字符编码，提供 UTF-8 兜底
        String encoding = getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            encoding = StandardCharsets.UTF_8.name();
        }

        return new BufferedReader(new InputStreamReader(this.getInputStream(), encoding));
    }
}