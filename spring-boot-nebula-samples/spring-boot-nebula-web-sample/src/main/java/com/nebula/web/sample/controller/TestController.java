package com.nebula.web.sample.controller;

import com.nebula.web.boot.annotation.NebulaResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wh
 * @date : 2023/8/2 10:28
 * @description:
 */
@RestController
@RequestMapping("/test/eventBus/v1")
public class TestController {

    
    @GetMapping("/test")
    @NebulaResponseBody
    public String test() {
        return "小奏";
    }
    
}
