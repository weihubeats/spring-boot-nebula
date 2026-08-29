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

import cn.idev.excel.write.handler.WriteHandler;
import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteWorkbookHolder;
import cn.idev.excel.write.merge.LoopMergeStrategy;
import cn.idev.excel.write.merge.OnceAbsoluteMergeStrategy;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import cn.idev.excel.write.style.column.SimpleColumnWidthStyleStrategy;

/**
 * 常用 Excel 写入样式/策略工厂, 供 {@link ExcelUtils#export} 的 {@code WriteHandler...} 参数使用。
 * <pre>
 * ExcelUtils.export(response, "file", "sheet", list, Vo.class,
 *     StylePresets.freezeHeader(), StylePresets.autoWidth());
 * </pre>
 */
public final class StylePresets {
    
    private StylePresets() {
    }
    
    /**
     * 冻结首行 (表头), 滚动时表头常驻。
     */
    public static WriteHandler freezeHeader() {
        return freezePane(0, 1);
    }
    
    /**
     * 通用冻结窗格。
     *
     * @param colSplit 冻结列数 (0=不冻结列)
     * @param rowSplit 冻结行数 (0=不冻结行)
     */
    public static WriteHandler freezePane(int colSplit, int rowSplit) {
        return new SheetWriteHandler() {
            
            @Override
            public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder,
                                         WriteSheetHolder writeSheetHolder) {
                writeSheetHolder.getSheet().createFreezePane(colSplit, rowSplit);
            }
        };
    }
    
    /**
     * 列宽自适应 (按内容最长匹配), 大数据量场景慎用 (需扫描单元格)。
     */
    public static WriteHandler autoWidth() {
        return new LongestMatchColumnWidthStyleStrategy();
    }
    
    /**
     * 固定列宽 (所有列统一宽度)。
     *
     * @param width 列宽 (1/256 字符宽度单位, POI 约定)
     */
    public static WriteHandler fixedWidth(int width) {
        return new SimpleColumnWidthStyleStrategy(width);
    }
    
    /**
     * 一次性绝对区域合并 (整片矩形区域合并为一个单元格)。
     */
    public static WriteHandler mergeOnce(int firstRow, int lastRow, int firstCol, int lastCol) {
        return new OnceAbsoluteMergeStrategy(firstRow, lastRow, firstCol, lastCol);
    }
    
    /**
     * 循环块合并: 在 {@code columnIndex} 列上, 每 {@code eachRows} 行合并为一个单元格 (固定间隔, 非按值合并)。
     *
     * @param eachRows   每块合并的行数, 必须 &gt;= 1
     * @param columnIndex 目标列索引 (0-based, 必须 &gt;= 0)
     */
    public static WriteHandler mergeLoop(int eachRows, int columnIndex) {
        return new LoopMergeStrategy(eachRows, columnIndex);
    }
}
