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
 
package com.nebula.mybatis.handler;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeException;

/**
 * @author : wh
 * @date : 2024/3/11 12:59
 * @description:
 */
@MappedJdbcTypes(JdbcType.ARRAY)
@MappedTypes({Integer[].class, String[].class, Boolean[].class, Double[].class, Long[].class, BigDecimal[].class})
@Slf4j
public class ArrayTypeHandler extends BaseTypeHandler<Object[]> {
    
    private static final String TYPE_NAME_VARCHAR = "varchar";
    private static final String TYPE_NAME_INTEGER = "integer";
    private static final String TYPE_NAME_BOOLEAN = "boolean";
    private static final String TYPE_NAME_NUMERIC = "numeric";
    private static final String TYPE_NAME_BIGINT = "bigint";
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object[] parameter,
                                    JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.length == 0) {
            ps.setNull(i, java.sql.Types.ARRAY);
            return;
        }
        
        String typeName = determineTypeName(parameter);
        if (typeName == null) {
            throw new TypeException("ArrayTypeHandler parameter typeName error, unsupported type: " + parameter.getClass().getName());
        }
        
        try {
            Connection conn = ps.getConnection();
            Array array = conn.createArrayOf(typeName, parameter);
            ps.setArray(i, array);
        } catch (SQLException e) {
            log.error("Error setting array parameter: " + Arrays.toString(parameter), e);
            throw e;
        }
    }
    
    @Override
    public Object[] getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return getArray(resultSet.getArray(columnName));
    }
    
    @Override
    public Object[] getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return getArray(resultSet.getArray(columnIndex));
    }
    
    @Override
    public Object[] getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return getArray(callableStatement.getArray(columnIndex));
    }
    
    /**
     * 确定数组的SQL类型名称
     *
     * @param parameter 数组参数
     * @return SQL类型名称
     */
    private String determineTypeName(Object[] parameter) {
        if (parameter instanceof Integer[]) {
            return TYPE_NAME_INTEGER;
        } else if (parameter instanceof String[]) {
            return TYPE_NAME_VARCHAR;
        } else if (parameter instanceof Boolean[]) {
            return TYPE_NAME_BOOLEAN;
        } else if (parameter instanceof Double[]) {
            return TYPE_NAME_NUMERIC;
        } else if (parameter instanceof Long[]) {
            return TYPE_NAME_BIGINT;
        } else if (parameter instanceof BigDecimal[]) {
            return TYPE_NAME_NUMERIC;
        }
        return null;
    }
    
    /**
     * 将SQL Array转换为Java数组
     *
     * @param array SQL Array
     * @return Java类型数组
     */
    private Object[] getArray(Array array) {
        if (array == null) {
            return null;
        }
        
        try {
            Object[] objArray = (Object[]) array.getArray();
            if (objArray == null || objArray.length == 0) {
                return objArray;
            }
            
            // 根据第一个非null元素的类型确定返回类型
            Class<?> componentType = determineComponentType(objArray);
            if (componentType == null) {
                // 如果无法确定类型，返回原始数组
                return objArray;
            }
            
            // 根据元素类型进行转换
            return convertArray(objArray, componentType);
        } catch (SQLException e) {
            log.error("Error converting SQL array to Java array", e);
            return null;
        } finally {
            try {
                array.free();
            } catch (SQLException e) {
                log.warn("Error freeing SQL array resource", e);
            }
        }
    }
    
    /**
     * 确定数组元素的类型
     *
     * @param objArray 原始数组
     * @return 元素类型
     */
    private Class<?> determineComponentType(Object[] objArray) {
        for (Object obj : objArray) {
            if (obj != null) {
                return obj.getClass();
            }
        }
        return null;
    }
    
    /**
     * 将数组转换为指定类型
     *
     * @param objArray      原始数组
     * @param componentType 目标元素类型
     * @return 转换后的数组
     */
    private Object[] convertArray(Object[] objArray, Class<?> componentType) {
        if (String.class.equals(componentType)) {
            return convertToStringArray(objArray);
        } else if (Integer.class.equals(componentType)) {
            return convertToIntegerArray(objArray);
        } else if (Boolean.class.equals(componentType)) {
            return convertToBooleanArray(objArray);
        } else if (Double.class.equals(componentType)) {
            return convertToDoubleArray(objArray);
        } else if (Long.class.equals(componentType)) {
            return convertToLongArray(objArray);
        } else if (BigDecimal.class.equals(componentType)) {
            return convertToBigDecimalArray(objArray);
        }
        
        // 默认返回原数组
        return objArray;
    }
    
    private String[] convertToStringArray(Object[] objArray) {
        String[] result = new String[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            result[i] = objArray[i] != null ? objArray[i].toString() : null;
        }
        return result;
    }
    
    private Integer[] convertToIntegerArray(Object[] objArray) {
        Integer[] result = new Integer[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            if (objArray[i] != null) {
                if (objArray[i] instanceof Integer) {
                    result[i] = (Integer) objArray[i];
                } else if (objArray[i] instanceof Number) {
                    result[i] = ((Number) objArray[i]).intValue();
                } else {
                    try {
                        result[i] = Integer.parseInt(objArray[i].toString());
                    } catch (NumberFormatException e) {
                        log.warn("Failed to convert value to Integer: " + objArray[i], e);
                        result[i] = null;
                    }
                }
            } else {
                result[i] = null;
            }
        }
        return result;
    }
    
    private Boolean[] convertToBooleanArray(Object[] objArray) {
        Boolean[] result = new Boolean[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            if (objArray[i] != null) {
                if (objArray[i] instanceof Boolean) {
                    result[i] = (Boolean) objArray[i];
                } else {
                    result[i] = Boolean.valueOf(objArray[i].toString());
                }
            } else {
                result[i] = null;
            }
        }
        return result;
    }
    
    private Double[] convertToDoubleArray(Object[] objArray) {
        Double[] result = new Double[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            if (objArray[i] != null) {
                if (objArray[i] instanceof Double) {
                    result[i] = (Double) objArray[i];
                } else if (objArray[i] instanceof Number) {
                    result[i] = ((Number) objArray[i]).doubleValue();
                } else {
                    try {
                        result[i] = Double.parseDouble(objArray[i].toString());
                    } catch (NumberFormatException e) {
                        log.warn("Failed to convert value to Double: " + objArray[i], e);
                        result[i] = null;
                    }
                }
            } else {
                result[i] = null;
            }
        }
        return result;
    }
    
    private Long[] convertToLongArray(Object[] objArray) {
        Long[] result = new Long[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            if (objArray[i] != null) {
                if (objArray[i] instanceof Long) {
                    result[i] = (Long) objArray[i];
                } else if (objArray[i] instanceof Number) {
                    result[i] = ((Number) objArray[i]).longValue();
                } else {
                    try {
                        result[i] = Long.parseLong(objArray[i].toString());
                    } catch (NumberFormatException e) {
                        log.warn("Failed to convert value to Long: " + objArray[i], e);
                        result[i] = null;
                    }
                }
            } else {
                result[i] = null;
            }
        }
        return result;
    }
    
    private BigDecimal[] convertToBigDecimalArray(Object[] objArray) {
        BigDecimal[] result = new BigDecimal[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            if (objArray[i] != null) {
                if (objArray[i] instanceof BigDecimal) {
                    result[i] = (BigDecimal) objArray[i];
                } else if (objArray[i] instanceof Number) {
                    result[i] = new BigDecimal(objArray[i].toString());
                } else {
                    try {
                        result[i] = new BigDecimal(objArray[i].toString());
                    } catch (NumberFormatException e) {
                        log.warn("Failed to convert value to BigDecimal: " + objArray[i], e);
                        result[i] = null;
                    }
                }
            } else {
                result[i] = null;
            }
        }
        return result;
    }
}
