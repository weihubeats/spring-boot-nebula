package com.nebula.web.sample;

import org.springframework.boot.SpringApplication;

import java.util.TimeZone;

/**
 * Hello world!
 *
 */
public class Application {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(Application.class, args);
    }
}
