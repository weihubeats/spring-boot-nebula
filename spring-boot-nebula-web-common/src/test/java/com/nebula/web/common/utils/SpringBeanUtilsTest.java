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

package com.nebula.web.common.utils;

import com.nebula.web.common.autoconfigure.NebulaWebCommonAutoConfiguration;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author : wh
 * @date : 2024/3/28 09:45
 * @description:
 */
@SpringBootTest(classes = SpringBeanUtilsTest.TestConfig.class)
public class SpringBeanUtilsTest {

    @Test
    public void getBean() {
        Object bean = SpringBeanUtils.getBean("testBean");
        assertInstanceOf(TestBean.class, bean);
        TestBean bean1 = SpringBeanUtils.getBean(TestBean.class);
        assertTrue(Objects.nonNull(bean1));
        assertThrowsExactly(NoSuchBeanDefinitionException.class, () -> SpringBeanUtils.getBean(NoTestBean.class));

    }

    static final class TestBean {
    }

    static final class NoTestBean {
    }

    @Configuration
    @EnableAutoConfiguration
    @ImportAutoConfiguration({NebulaWebCommonAutoConfiguration.class})
    public static class TestConfig {

        @Bean
        public TestBean testBean() {
            return new TestBean();

        }

    }

}