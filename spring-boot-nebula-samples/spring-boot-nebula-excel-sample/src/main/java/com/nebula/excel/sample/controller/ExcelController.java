package com.nebula.excel.sample.controller;

import com.nebula.excel.ExcelUtils;
import com.nebula.excel.sample.vo.XiaoZouVO;
import javax.servlet.http.HttpServletResponse;
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
