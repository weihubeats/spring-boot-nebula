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
 
package com.nebula.distribute.lock.sample.service;

import com.nebula.distribute.lock.annotation.NebulaDistributedLock;
import com.nebula.distribute.lock.sample.dto.OrderDTO;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2024/3/16 13:55
 * @description:
 */
@Service
public class TestService {
    
    @NebulaDistributedLock(lockNamePre = "order:updateOrder:", lockNamePost = "#dto.orderId")
    public void updateOrder(OrderDTO dto) {
        System.out.println("处理器请求中");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("请求处理完成");
    }
    
}
