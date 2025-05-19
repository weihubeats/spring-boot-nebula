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
public class ArrayTypeHandler extends BaseTypeHandler<Object[]> {
    
    private static final String TYPE_NAME_VARCHAR = "varchar";
    private static final String TYPE_NAME_INTEGER = "integer";
    private static final String TYPE_NAME_BOOLEAN = "boolean";
    private static final String TYPE_NAME_NUMERIC = "numeric";
    private static final String TYPE_NAME_BIGINT = "bigint";
    
    @Override
    public void setNonNullParameter(
                                    PreparedStatement ps, int i, Object[] parameter, JdbcType jdbcType) throws SQLException {
        String typeName = null;
        if (parameter instanceof Integer[]) {
            typeName = TYPE_NAME_INTEGER;
        } else if (parameter instanceof String[]) {
            typeName = TYPE_NAME_VARCHAR;
        } else if (parameter instanceof Boolean[]) {
            typeName = TYPE_NAME_BOOLEAN;
        } else if (parameter instanceof Double[]) {
            typeName = TYPE_NAME_NUMERIC;
        } else if (parameter instanceof Long[]) {
            typeName = TYPE_NAME_BIGINT;
        } else if (parameter instanceof BigDecimal[]) {
            typeName = TYPE_NAME_NUMERIC;
        }
        
        if (typeName == null) {
            throw new TypeException("ArrayTypeHandler parameter typeName error, your type is " + parameter.getClass().getName());
        }
        
        Connection conn = ps.getConnection();
        Array array = conn.createArrayOf(typeName, parameter);
        ps.setArray(i, array);
    }
    
    @Override
    public Object[] getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return getArray(resultSet.getArray(s));
    }
    
    @Override
    public Object[] getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return getArray(resultSet.getArray(i));
    }
    
    @Override
    public Object[] getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return getArray(callableStatement.getArray(i));
    }

    private Object[] getArray(Array array) {
        if (array == null) {
            return null;
        }
        try {
            Object[] objArray = (Object[]) array.getArray();

            // 检查并转换为正确类型的数组
            if (objArray != null && objArray.length > 0) {
                // 根据第一个元素的类型决定返回什么类型的数组
                if (objArray[0] instanceof String) {
                    String[] strArray = new String[objArray.length];
                    for (int i = 0; i < objArray.length; i++) {
                        strArray[i] = (String) objArray[i];
                    }
                    return strArray;
                }
                // 处理其他类型...类似代码
            }
            return objArray;
        } catch (Exception e) {
            //log.error("Error when converting SQL array to Java array", e);
        }
        return null;
    }
    
}
