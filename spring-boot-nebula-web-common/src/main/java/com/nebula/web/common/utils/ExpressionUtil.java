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

import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author : wh
 * @date : 2024/3/16 10:20
 * @description:
 */
public class ExpressionUtil {
    
    private static final ExpressionParser parser = new SpelExpressionParser();
    
    private static final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
    
    /**
     * 解析EL表达式
     *
     * @param expressionString 表达式字符串
     * @param method           方法
     * @param args             参数
     * @return 解析结果
     */
    public static Object parse(String expressionString, Method method, Object[] args) {
        if (isEmpty(expressionString)) {
            return null;
        }
        EvaluationContext context = createEvaluationContext(method, args);
        return parseExpression(expressionString, context);
    }
    
    /**
     * 解析EL表达式
     *
     * @param expressionString 表达式字符串
     * @param rootObject       根对象
     * @return 解析结果
     */
    public static Object parse(String expressionString, Object rootObject) {
        if (isEmpty(expressionString)) {
            return null;
        }
        return parseExpression(expressionString, rootObject);
    }
    
    /**
     * 解析EL表达式
     *
     * @param expressionString 表达式字符串
     * @param variables        变量Map
     * @return 解析结果
     */
    public static Object parse(String expressionString, Map<String, Object> variables) {
        if (isEmpty(expressionString)) {
            return null;
        }
        EvaluationContext context = createEvaluationContext(variables);
        return parseExpression(expressionString, context);
    }
    
    /**
     * 判断是否为EL表达式
     *
     * @param param 待判断的字符串
     * @return 是否为EL表达式
     */
    public static boolean isEl(String param) {
        return !isEmpty(param) && param.startsWith("#");
    }
    
    /**
     * 创建EvaluationContext
     *
     * @param method 方法
     * @param args   参数
     * @return EvaluationContext
     */
    private static EvaluationContext createEvaluationContext(Method method, Object[] args) {
        String[] paramNames = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return context;
    }
    
    /**
     * 创建EvaluationContext
     *
     * @param variables 变量Map
     * @return EvaluationContext
     */
    private static EvaluationContext createEvaluationContext(Map<String, Object> variables) {
        EvaluationContext context = new StandardEvaluationContext();
        variables.forEach(context::setVariable);
        return context;
    }
    
    /**
     * 解析表达式
     *
     * @param expressionString 表达式字符串
     * @param context          EvaluationContext
     * @return 解析结果
     */
    private static Object parseExpression(String expressionString, EvaluationContext context) {
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(context);
    }
    
    /**
     * 解析表达式
     *
     * @param expressionString 表达式字符串
     * @param rootObject       根对象
     * @return 解析结果
     */
    private static Object parseExpression(String expressionString, Object rootObject) {
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(rootObject);
    }
    
    /**
     * 判断字符串是否为空
     *
     * @param str 待判断的字符串
     * @return 是否为空
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
