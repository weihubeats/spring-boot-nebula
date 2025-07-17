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
 
package com.nebula.excel.sample.controller;

import com.nebula.excel.ExcelUtils;
import com.nebula.excel.sample.vo.XiaoZouVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wh
 * @date : 2025/6/25
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class ExcelController {
    
    @GetMapping("excel/export")
    public void exportExcel(boolean isNull, HttpServletResponse response) {
        if (isNull) {
            ExcelUtils.export(response, "测试导出", null, XiaoZouVO.class);
            return;
        }
        ExcelUtils.export(response, "测试导出", XiaoZouVO.builders(), XiaoZouVO.class);
    }
    
    @GetMapping("excel/export-with-date-suffix")
    public void exportWithDateSuffix(HttpServletResponse response) {
        ExcelUtils.exportWithDateSuffix(response, "测试导出", XiaoZouVO.builders(), XiaoZouVO.class);
    }
}
