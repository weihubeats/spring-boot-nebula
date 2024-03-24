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
 
package com.nebula.base.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : wh
 * @date : 2023/12/18 13:57
 * @description:
 */
public class ReflectionUtils {
    
    private static final String METHOD = "writeReplace";
    
    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);
    
    /**
     * 获取 Function 名
     * @param func
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> String getFieldName(PropertyFunc<T, R> func) {
        try {
            // 通过获取对象方法，判断是否存在该方法
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            // 利用jdk的SerializedLambda 解析方法引用
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String getter = serializedLambda.getImplMethodName();
            return resolveFieldName(getter);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String resolveFieldName(String getMethodName) {
        if (getMethodName.startsWith("get")) {
            getMethodName = getMethodName.substring(3);
        } else if (getMethodName.startsWith("is")) {
            getMethodName = getMethodName.substring(2);
        }
        // 小写第一个字母
        return firstToLowerCase(getMethodName);
    }
    
    private static String firstToLowerCase(String param) {
        if (DataUtils.isEmpty(param)) {
            return "";
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }
    
    /**
     * class 是否存在属性
     * @param fieldName 属性名
     * @param clazz     类
     * @return
     */
    public static <T> boolean isExistFieldName(String fieldName, Class<T> clazz) {
        boolean flag = false;
        // 获取这个类的所有属性
        Field[] fields = clazz.getDeclaredFields();
        // 循环遍历所有的fields
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
    
    public static <T, V extends Annotation> boolean isExistFieldName(String fieldName, Class<T> clazz, Class<V> annotationClass) {
        boolean flag = false;
        // 获取这个类的所有属性
        Field[] fields = clazz.getDeclaredFields();
        // 循环遍历所有的fields
        for (Field field : fields) {
            if (field.getName().equals(fieldName) && Objects.nonNull(field.getAnnotation(annotationClass))) {
                flag = true;
                break;
            }
        }
        return flag;
    }
    
    /**
     * 获取指定属性
     *
     * @param object
     * @param fieldName
     * @return
     * @throws Exception
     */
    public static Object getPropertyValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            logger.error("getPropertyValue exception", e);
            return null;
        }
    }
    
    /**
     * 设置属性值
     *
     * @param object
     * @param fieldName
     * @param value
     */
    public static void setPropertyValue(Object object, String fieldName, Object value) {
        try {
            Field f = object.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(object, value);
        } catch (Exception e) {
            logger.error("setPropertyValue exception", e);
        }
    }
    
    /**
     * 获取构造器
     * @param clazzPath
     * @param parameterTypes
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public static Constructor getConstructor(String clazzPath, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> operationClazz = Class.forName(clazzPath);
        return operationClazz.getDeclaredConstructor(parameterTypes);
    }
    
    /**
     * 根据Getter方法拿到字段名
     */
    public static Optional<String> getFieldNameByGetter(Method method) throws IntrospectionException {
        Class<?> clazz = method.getDeclaringClass();
        BeanInfo info = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] props = info.getPropertyDescriptors();
        for (PropertyDescriptor pd : props) {
            if (!method.equals(pd.getWriteMethod()) && !method.equals(pd.getReadMethod())) {
                continue;
            }
            return Optional.ofNullable(pd.getName());
        }
        return Optional.empty();
    }
    
}
