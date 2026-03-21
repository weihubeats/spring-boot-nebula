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
 
package com.nebula.join.interceptor;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

class ColumnQualifier extends ExpressionVisitorAdapter {
    
    private final String alias;
    
    public ColumnQualifier(String alias) {
        this.alias = alias;
    }
    
    @Override
    public void visit(Column column) {
        String columnName = column.getColumnName();
        // 1. 检查列名是否是 SQL 关键字/常量
        if (columnName != null) {
            String upperName = columnName.toUpperCase();
            // 如果列名是 TRUE, FALSE, NULL (通常不应被限定的常量)
            if (upperName.equals("TRUE") || upperName.equals("FALSE") || upperName.equals("NULL")) {
                return;
            }
        }
        // 2. 检查列是否已被限定 (即是否已有表名或别名)
        boolean isUnqualified = column.getTable() == null || column.getTable().getName() == null;
        // 3. 如果是真正的、未限定的列，则设置主表别名
        if (isUnqualified) {
            column.setTable(new Table(alias));
        }
    }
}
