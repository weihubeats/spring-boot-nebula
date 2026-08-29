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
 
package com.nebula.excel;

/**
 * 单行导入失败信息。
 *
 * @param rowIndex      行号 (0-based, 与 EasyExcel AnalysisContext 一致)
 * @param rowData       原始行数据字符串表示, 便于排查
 * @param errorMessage  失败原因 (校验异常或解析异常 message)
 */
public record RowError(int rowIndex, String rowData, String errorMessage) {
}
