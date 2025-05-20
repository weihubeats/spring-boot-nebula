package com.nebula.mybatis.handler;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeException;

/**
 * @author : wh
 * @date : 2025/5/14
 * @description:
 */
@MappedJdbcTypes(JdbcType.ARRAY)
@MappedTypes(List.class)
public class ListTypeHandler extends BaseTypeHandler<List<?>> {

    private static final String TYPE_NAME_VARCHAR = "varchar";
    private static final String TYPE_NAME_INTEGER = "integer";
    private static final String TYPE_NAME_BOOLEAN = "boolean";
    private static final String TYPE_NAME_NUMERIC = "numeric";
    private static final String TYPE_NAME_BIGINT = "bigint";
    private static final String TYPE_NAME_DOUBLE_PRECISION = "double precision";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<?> parameter,
        JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setNull(i, java.sql.Types.ARRAY);
            return;
        }

        Object firstElement = parameter.get(0);
        String typeName = null;
        Object[] arrayForDb = null;

        if (firstElement instanceof Integer) {
            typeName = TYPE_NAME_INTEGER;
            arrayForDb = parameter.toArray(new Integer[0]);
        } else if (firstElement instanceof String) {
            typeName = TYPE_NAME_VARCHAR;
            arrayForDb = parameter.toArray(new String[0]);
        } else if (firstElement instanceof Boolean) {
            typeName = TYPE_NAME_BOOLEAN;
            arrayForDb = parameter.toArray(new Boolean[0]);
        } else if (firstElement instanceof Double) {

            typeName = TYPE_NAME_DOUBLE_PRECISION; 
            arrayForDb = parameter.toArray(new Double[0]);
        } else if (firstElement instanceof Long) {
            typeName = TYPE_NAME_BIGINT;
            arrayForDb = parameter.toArray(new Long[0]);
        } else if (firstElement instanceof BigDecimal) {
            typeName = TYPE_NAME_NUMERIC;
            arrayForDb = parameter.toArray(new BigDecimal[0]);
        }

        if (typeName == null) {
            throw new TypeException("ListTypeHandler parameter typeName error, unsupported element type: " + firstElement.getClass().getName());
        }

        Connection conn = ps.getConnection();
        // 确保你的数据库驱动支持 createArrayOf 方法，并且类型名称与数据库定义一致
        // 例如 PostgreSQL, Oracle 支持良好。MySQL 对 ARRAY 的原生支持有限，通常通过其他方式模拟。
        Array array = conn.createArrayOf(typeName, arrayForDb);
        ps.setArray(i, array);
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getListFromSqlArray(rs.getArray(columnName));
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getListFromSqlArray(rs.getArray(columnIndex));
    }

    @Override
    public List<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getListFromSqlArray(cs.getArray(columnIndex));
    }

    private List<?> getListFromSqlArray(Array array) {
        if (array == null) {
            return null;
        }
        try {
            Object resultArray = array.getArray();
            if (resultArray == null) {
                return null;
            }
            return Arrays.asList((Object[]) resultArray);
        } catch (SQLException e) {
            throw new RuntimeException("Error converting SQL Array to List", e);
        } finally {
            try {
                array.free();
            } catch (SQLException ignored) {
            }
        }
    }
}
