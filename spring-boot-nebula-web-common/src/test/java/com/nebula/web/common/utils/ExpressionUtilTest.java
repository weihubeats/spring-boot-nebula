package com.nebula.web.common.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.SpelParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author : wh
 * @date : 2024/9/13 16:19
 * @description:
 */
public class ExpressionUtilTest {

    @Test
    void testParseWithMethodAndArgs() throws NoSuchMethodException {
        Method method = TestClass.class.getDeclaredMethod("testMethod", String.class, int.class);
        Object[] args = {"Hello", 5};

        assertEquals("Hello1", ExpressionUtil.parse("#param1", method, args));
        assertEquals(5, ExpressionUtil.parse("#param2", method, args));
        assertEquals("Hello51", ExpressionUtil.parse("#param1 + #param2", method, args));
    }

    @Test
    void testParseWithRootObject() {
        TestClass testObj = new TestClass("World", 10);

        assertEquals("World", ExpressionUtil.parse("name", testObj));
        assertEquals(10, ExpressionUtil.parse("age", testObj));
        assertEquals("World is 10 years old", ExpressionUtil.parse("name + ' is ' + age + ' years old'", testObj));
    }

    @Test
    void testParseWithVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", 10);
        variables.put("y", 20);

        assertEquals(30, ExpressionUtil.parse("#x + #y", variables));
        assertEquals(200, ExpressionUtil.parse("#x * #y", variables));
    }

    @Test
    void testIsEl() {
        assertTrue(ExpressionUtil.isEl("#expression"));
        assertFalse(ExpressionUtil.isEl("normalString"));
        assertFalse(ExpressionUtil.isEl(""));
        assertFalse(ExpressionUtil.isEl(null));
    }

    @Test
    void testParseWithInvalidExpression() {
        assertThrows(SpelParseException.class, () -> ExpressionUtil.parse("invalid#expression", new Object()));
    }

    @Test
    void testParseWithEmptyExpression() {
        assertNull(ExpressionUtil.parse("", new Object()));
        assertNull(ExpressionUtil.parse(null, new Object()));
    }

    // 用于测试的内部类
    private static class TestClass {
        private String name;
        private int age;

        public TestClass(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public void testMethod(String param1, int param2) {
            // 方法体为空，仅用于测试
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}