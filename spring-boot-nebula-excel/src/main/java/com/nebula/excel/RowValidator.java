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
 * 行级校验器。在 {@link ExcelUtils#read} 流程中对每行数据做业务校验。
 * 抛出的任何异常会被捕获并记入 {@link RowError}, 不中断后续行解析。
 */
@FunctionalInterface
public interface RowValidator<T> {
    
    /**
     * @param rowIndex 行号 (0-based)
     * @param row      已解析的行数据
     * @throws Exception 校验失败时抛出, message 会作为 RowError.errorMessage
     */
    void validate(int rowIndex, T row) throws Exception;
}
