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
 
package com.nebula.base.utils.juc;

import com.google.common.collect.Lists;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author : wh
 * @date : 2025/6/19
 * @description:
 */
class CompletableFutureUtilsTest {
    
    StudentService service = new StudentService();
    
    ArrayList<Long> ids = Lists.newArrayList(1L, 2L);
    
    @Test
    @DisplayName("单个查询成功获取结果")
    void testSupplyAndGet_Success() {
        List<Student> students = CompletableFutureUtils.supplyAndGet(() -> service.getStudentByIds(ids));
        
    }
    
    @Test
    void allSupplyAndGet() {
        StudentService service = new StudentService();
        ArrayList<Long> ids = Lists.newArrayList(1L, 2L);
        
        Tuple2<List<Student>, List<Teacher>> result = CompletableFutureUtils.allSupplyAndGet(
                () -> service.getStudentByIds(ids),
                () -> service.getTeacherByIds(ids));
        
        List<Student> left = result._1();
        List<Teacher> right = result._2();
        assertNotNull(result);
        assertNotNull(left);
        assertNotNull(right);
        
    }
    
    static class StudentService {
        
        List<Student> getStudentByIds(List<Long> ids) {
            System.out.println("查询数据库中");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("查询完成");
            return new ArrayList<>();
        }
        
        List<Teacher> getTeacherByIds(List<Long> ids) {
            System.out.println("查询数据库中");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("查询完成");
            return new ArrayList<>();
        }
        
    }
    
    static class Student {
        
    }
    
    static class Teacher {
        
    }
}