package com.nebula.web.boot.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.util.StreamUtils;

public class RepeatableReadRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] bodyCache;

    public RepeatableReadRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 在构造器中一次性把原生的流读取到内存缓存中
        this.bodyCache = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        // 每次调用都返回一个基于缓存数组构建的新流
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
                // 空实现即可
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // 将缓存的流包装成 Reader，注意字符编码的传递
        return new BufferedReader(new InputStreamReader(this.getInputStream(), getCharacterEncoding()));
    }
}
