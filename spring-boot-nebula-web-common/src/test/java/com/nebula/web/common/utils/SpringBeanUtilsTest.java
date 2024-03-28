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
        assertTrue(bean instanceof TestBean);
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