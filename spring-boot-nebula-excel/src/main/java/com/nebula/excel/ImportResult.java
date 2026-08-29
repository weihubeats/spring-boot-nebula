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

import java.util.List;

/**
 * Excel 导入结果。包含成功行与失败行, 用于「部分成功」场景。
 *
 * @param success   成功解析并通过 (可选) 校验的行
 * @param failures  失败行 (解析异常或校验失败), 不为 null
 * @param totalRows 读取到的总数据行数 (不含表头)
 * @param <T>       行数据泛型
 */
public record ImportResult<T>(List<T> success, List<RowError> failures, int totalRows) {
}
