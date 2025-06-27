package com.nebula.excel.sample.vo;

import cn.idev.excel.annotation.ExcelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author : wh
 * @date : 2025/6/25
 * @description:
 */
@Data
public class XiaoZouVO {

    @ExcelProperty("id")
    private Long id;
    
    @ExcelProperty("名字")
    private String name;

    public static List<XiaoZouVO> builders() {
        List<XiaoZouVO> res = new ArrayList<>();

        XiaoZouVO xiaoZouVO = new XiaoZouVO(1L, "小奏");

        XiaoZouVO xiaoZouVO1 = new XiaoZouVO(2L, "weihubeats");

        res.add(xiaoZouVO);
        res.add(xiaoZouVO1);
        return res;
    }

    public XiaoZouVO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
