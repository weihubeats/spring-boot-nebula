package com.nebula.distribute.lock.sample.controller;

import com.nebula.distribute.lock.sample.service.TestService;
import com.nebula.web.boot.annotation.NebulaResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wh
 * @date : 2023/8/2 10:28
 * @description:
 */
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class TestController {
    
    private final TestService testServcie;

    
    @GetMapping("/test")
    @NebulaResponseBody
    public String test() throws Exception{
        testServcie.test();
        return "小奏";
    }
    
}
