package com.nebula.distribute.lock.sample.service;

import com.nebula.distribute.lock.annotation.NebulaDistributedLock;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2024/3/16 13:55
 * @description:
 */
@Service
public class TestService {

    @NebulaDistributedLock(lockName = "wh-test")
    public void test() {
        System.out.println("处理器请求中");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("请求处理完成");
    }
    
}
