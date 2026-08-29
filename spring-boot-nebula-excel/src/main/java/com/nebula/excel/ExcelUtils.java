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

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AbstractIgnoreExceptionReadListener;
import cn.idev.excel.read.listener.PageReadListener;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.handler.WriteHandler;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.fill.FillConfig;
import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.TimeUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 读写工具。基于 fastexcel (easyexcel 分支)。
 *
 * <p><b>OutputStream 所有权约定</b>: 所有以 {@link OutputStream} 为 sink 的新方法均设置
 * {@code autoCloseStream=false}, 调用方负责关闭流 (try-with-resources)。HTTP 响应式方法内部
 * 已自行 try-with-resources 关闭 response 输出流。
 *
 * <p><b>异常约定</b>: 写入/读取失败统一包装为 {@link RuntimeException} 并带业务上下文, 调用方
 * 按需捕获; 不抛 checked {@link IOException}。
 *
 * @author wh
 */
@Slf4j
public class ExcelUtils {
    
    /** XLSX 单 sheet 最大行数 (1,048,576)。auto-split 默认上限。 */
    public static final int XLSX_MAX_ROWS_PER_SHEET = 1_048_576;
    
    // ==================== HTTP 响应 Header ====================
    
    /**
     * 为 HTTP 响应设置 Excel 导出标准 Header (默认 XLSX)。
     */
    public static void buildResponse(HttpServletResponse response, String fileName) {
        buildResponse(response, fileName, ExcelTypeEnum.XLSX);
    }
    
    /**
     * 为 HTTP 响应设置导出 Header, 按 {@code type} 选择后缀与 Content-Type。
     *
     * @param type CSV / XLS / XLSX
     */
    public static void buildResponse(HttpServletResponse response, String fileName, ExcelTypeEnum type) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String contentType = switch (type) {
            case CSV -> "text/csv";
            case XLS -> "application/vnd.ms-excel";
            default -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        };
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encoded + type.getValue());
    }
    
    /**
     * 添加 Excel 文件后缀 (默认 XLSX)。
     */
    public static String convert2FileName(String fileNameCode) {
        return convert2FileName(fileNameCode, ExcelTypeEnum.XLSX);
    }
    
    /**
     * 按指定类型添加文件后缀。
     */
    public static String convert2FileName(String fileNameCode, ExcelTypeEnum type) {
        return fileNameCode + type.getValue();
    }
    
    // ==================== 注解表头导出 (Class<?>) ====================
    
    /**
     * 导出单 Sheet 到指定 OutputStream (调用方负责关闭流)。
     *
     * @param os        目标流
     * @param sheetName sheet 名
     * @param list      数据 (null 视为空, 仅写表头)
     * @param head      表头注解类
     * @param type      文件类型 (XLSX/XLS/CSV)
     * @param handlers  可选额外 WriteHandler (样式/合并/冻结等, 见 {@link StylePresets})
     */
    public static <T> void export(OutputStream os, String sheetName, List<T> list, Class<T> head,
                                  ExcelTypeEnum type, WriteHandler... handlers) {
        var builder = EasyExcel.write(os, head).excelType(type).autoCloseStream(false);
        for (WriteHandler h : handlers) {
            builder.registerWriteHandler(h);
        }
        try {
            builder.sheet(sheetName).doWrite(list == null ? Collections.emptyList() : list);
        } catch (RuntimeException e) {
            log.error("Excel export failed, sheetName: {}, type: {}", sheetName, type, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    /** {@link #export(OutputStream, String, List, Class, ExcelTypeEnum, WriteHandler...)} 的 XLSX 简化版。 */
    public static <T> void export(OutputStream os, String sheetName, List<T> list, Class<T> head,
                                  WriteHandler... handlers) {
        export(os, sheetName, list, head, ExcelTypeEnum.XLSX, handlers);
    }
    
    /**
     * 导出单 Sheet HTTP 响应 (XLSX)。
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> list,
                                  Class<T> head) {
        buildResponse(response, fileName, ExcelTypeEnum.XLSX);
        try (OutputStream os = response.getOutputStream()) {
            export(os, sheetName, list, head, ExcelTypeEnum.XLSX);
        } catch (IOException e) {
            log.error("Excel export failed, fileName: {}, sheetName: {}", fileName, sheetName, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    /**
     * 导出单 Sheet HTTP 响应, sheetName = fileName。
     */
    public static <T> void export(HttpServletResponse response, String fileName, List<T> list,
                                  Class<T> head) {
        export(response, fileName, fileName, list, head);
    }
    
    /**
     * 导出单 Sheet HTTP 响应并附加当前日期时间到文件名, 返回完整文件名。
     */
    public static <T> String exportWithDateSuffix(HttpServletResponse response, String fileName,
                                                  List<T> list, Class<T> head) {
        return exportWithDateSuffix(response, fileName, fileName, list, head);
    }
    
    /**
     * 导出单 Sheet HTTP 响应并附加当前日期时间到文件名, 返回完整文件名。
     */
    public static <T> String exportWithDateSuffix(HttpServletResponse response, String fileName, String sheetName,
                                                  List<T> list, Class<T> head) {
        String dateTimeSuffix = TimeUtil.formatCurrentDateTime(TimeUtil.YYYYMMDDHHMMSS);
        String fullFileName = String.join("-", fileName, dateTimeSuffix);
        export(response, fullFileName, sheetName, list, head);
        return fullFileName;
    }
    
    /**
     * 导出 CSV HTTP 响应。
     */
    public static <T> void exportCsv(HttpServletResponse response, String fileName, String sheetName,
                                     List<T> list, Class<T> head) {
        buildResponse(response, fileName, ExcelTypeEnum.CSV);
        try (OutputStream os = response.getOutputStream()) {
            export(os, sheetName, list, head, ExcelTypeEnum.CSV);
        } catch (IOException e) {
            log.error("CSV export failed, fileName: {}", fileName, e);
            throw new RuntimeException("CSV export failed", e);
        }
    }
    
    /**
     * 导出到文件 (覆盖写入), 返回传入 file。
     */
    public static <T> File exportToFile(File file, String sheetName, List<T> list, Class<T> head,
                                        ExcelTypeEnum type, WriteHandler... handlers) {
        Objects.requireNonNull(file, "file must not be null");
        try (OutputStream os = new FileOutputStream(file)) {
            export(os, sheetName, list, head, type, handlers);
        } catch (IOException e) {
            log.error("Excel export to file failed: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Excel export to file failed", e);
        }
        return file;
    }
    
    /** {@link #exportToFile} 的 XLSX 简化版。 */
    public static <T> File exportToFile(File file, String sheetName, List<T> list, Class<T> head,
                                        WriteHandler... handlers) {
        return exportToFile(file, sheetName, list, head, ExcelTypeEnum.XLSX, handlers);
    }
    
    /**
     * 导出为字节数组 (内存缓冲, 适合中小数据量 / 异步上传 OSS/邮件附件)。
     */
    public static <T> byte[] exportToBytes(String sheetName, List<T> list, Class<T> head,
                                           ExcelTypeEnum type, WriteHandler... handlers) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            export(bos, sheetName, list, head, type, handlers);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Excel export to bytes failed", e);
            throw new RuntimeException("Excel export to bytes failed", e);
        }
    }
    
    /** {@link #exportToBytes} 的 XLSX 简化版。 */
    public static <T> byte[] exportToBytes(String sheetName, List<T> list, Class<T> head,
                                           WriteHandler... handlers) {
        return exportToBytes(sheetName, list, head, ExcelTypeEnum.XLSX, handlers);
    }
    
    // ==================== 动态表头导出 ====================
    
    /**
     * 动态表头导出。运行时构造列名, 无需注解类。每行数据为 {@code List<Object>} (按列顺序)。
     *
     * @param os     目标流 (调用方关闭)
     * @param head   表头, 外层 List 每项为一列, 内层 List 为该列多级表头 (单级则放一个元素)
     * @param data   数据行, 每行一个 List, 元素顺序与 head 列顺序一致
     */
    public static void export(OutputStream os, String sheetName, List<List<String>> head,
                              List<List<Object>> data, ExcelTypeEnum type, WriteHandler... handlers) {
        var builder = EasyExcel.write(os).head(head).excelType(type).autoCloseStream(false);
        for (WriteHandler h : handlers) {
            builder.registerWriteHandler(h);
        }
        try {
            builder.sheet(sheetName).doWrite(data == null ? Collections.emptyList() : data);
        } catch (RuntimeException e) {
            log.error("Excel dynamic-head export failed, sheetName: {}", sheetName, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    /** {@link #export(OutputStream, String, List, List, ExcelTypeEnum, WriteHandler...)} 的 HTTP 版。 */
    public static void export(HttpServletResponse response, String fileName, String sheetName,
                              List<List<String>> head, List<List<Object>> data) {
        buildResponse(response, fileName, ExcelTypeEnum.XLSX);
        try (OutputStream os = response.getOutputStream()) {
            export(os, sheetName, head, data, ExcelTypeEnum.XLSX);
        } catch (IOException e) {
            log.error("Excel dynamic-head export failed, fileName: {}", fileName, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    // ==================== 流式写入 (Iterator, 低内存) ====================
    
    /**
     * 基于 {@link Iterator} 的流式单 sheet 导出, 避免全量数据入内存 (OOM-safe)。
     *
     * @param data      数据迭代器
     * @param batchSize 每批拉取行数, 影响吞吐与内存占用 (建议 500~2000)
     */
    public static <T> void export(OutputStream os, String sheetName, Iterator<T> data, Class<T> head,
                                  int batchSize, ExcelTypeEnum type, WriteHandler... handlers) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be > 0");
        }
        var builder = EasyExcel.write(os, head).excelType(type).autoCloseStream(false);
        for (WriteHandler h : handlers) {
            builder.registerWriteHandler(h);
        }
        try (ExcelWriter writer = builder.build()) {
            WriteSheet sheet = EasyExcel.writerSheet(sheetName).build();
            while (data.hasNext()) {
                List<T> batch = new ArrayList<>(batchSize);
                for (int i = 0; i < batchSize && data.hasNext(); i++) {
                    batch.add(data.next());
                }
                writer.write(batch, sheet);
            }
        } catch (RuntimeException e) {
            log.error("Excel stream export failed, sheetName: {}", sheetName, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    /** {@link #export(OutputStream, String, Iterator, Class, int, ExcelTypeEnum, WriteHandler...)} 的 XLSX 版。 */
    public static <T> void export(OutputStream os, String sheetName, Iterator<T> data, Class<T> head,
                                  int batchSize, WriteHandler... handlers) {
        export(os, sheetName, data, head, batchSize, ExcelTypeEnum.XLSX, handlers);
    }
    
    // ==================== 大数据 auto-split 多 sheet ====================
    
    /**
     * 大数据流式导出: 超过 {@code maxRowsPerSheet} 自动切到下一个 sheet, 规避单 sheet 行上限。
     * OOM-safe (迭代器拉批)。
     *
     * @param maxRowsPerSheet 单 sheet 行上限 (XLSX 上限 {@link #XLSX_MAX_ROWS_PER_SHEET})
     * @param batchSize       拉批大小, 必须 {@code <= maxRowsPerSheet}
     */
    public static <T> void exportAutoSplit(OutputStream os, String baseSheetName, Iterator<T> data,
                                           Class<T> head, int maxRowsPerSheet, int batchSize,
                                           ExcelTypeEnum type, WriteHandler... handlers) {
        if (maxRowsPerSheet <= 0) {
            throw new IllegalArgumentException("maxRowsPerSheet must be > 0");
        }
        if (batchSize <= 0 || batchSize > maxRowsPerSheet) {
            throw new IllegalArgumentException("batchSize must be in (0, maxRowsPerSheet]");
        }
        var builder = EasyExcel.write(os, head).excelType(type).autoCloseStream(false);
        for (WriteHandler h : handlers) {
            builder.registerWriteHandler(h);
        }
        try (ExcelWriter writer = builder.build()) {
            int sheetIndex = 0;
            int rowsInSheet = 0;
            WriteSheet sheet = EasyExcel.writerSheet(sheetIndex, baseSheetName + "_" + (sheetIndex + 1)).build();
            while (data.hasNext()) {
                List<T> batch = new ArrayList<>(batchSize);
                for (int i = 0; i < batchSize && data.hasNext(); i++) {
                    batch.add(data.next());
                }
                if (rowsInSheet + batch.size() > maxRowsPerSheet) {
                    sheetIndex++;
                    sheet = EasyExcel.writerSheet(sheetIndex, baseSheetName + "_" + (sheetIndex + 1)).build();
                    rowsInSheet = 0;
                }
                writer.write(batch, sheet);
                rowsInSheet += batch.size();
            }
        } catch (RuntimeException e) {
            log.error("Excel auto-split export failed, baseSheetName: {}", baseSheetName, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    /**
     * List 版 auto-split (in-memory 分片), sheet 命名 {@code baseSheetName_1/2/...}。
     */
    public static <T> void exportAutoSplit(OutputStream os, String baseSheetName, List<T> list,
                                           Class<T> head, int maxRowsPerSheet, ExcelTypeEnum type,
                                           WriteHandler... handlers) {
        List<T> data = list == null ? Collections.emptyList() : list;
        int batchSize = Math.min(maxRowsPerSheet, 1000);
        exportAutoSplit(os, baseSheetName, data.iterator(), head, maxRowsPerSheet, batchSize, type, handlers);
    }
    
    /** {@link #exportAutoSplit(OutputStream, String, Iterator, Class, int, int, ExcelTypeEnum, WriteHandler...)} 的 HTTP 版。 */
    public static <T> void exportAutoSplit(HttpServletResponse response, String fileName, String baseSheetName,
                                           Iterator<T> data, Class<T> head, int maxRowsPerSheet,
                                           int batchSize, WriteHandler... handlers) {
        buildResponse(response, fileName, ExcelTypeEnum.XLSX);
        try (OutputStream os = response.getOutputStream()) {
            exportAutoSplit(os, baseSheetName, data, head, maxRowsPerSheet, batchSize, ExcelTypeEnum.XLSX, handlers);
        } catch (IOException e) {
            log.error("Excel auto-split export failed, fileName: {}", fileName, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    // ==================== 多 sheet 导出 ====================
    
    /**
     * 导出包含多个 Sheet 的 Excel (共享同一表头类)。
     * 若 sheetNames 与 data 数量不匹配则抛 {@link IllegalArgumentException}。
     *
     * @throws IOException 历史签名保留; 实际由 fastexcel 内部抛出
     */
    public static <T> void exportMultiSheet(HttpServletResponse response, String fileName, List<String> sheetNames,
                                            List<List<T>> data, Class<T> head) throws IOException {
        int dataSize = (data == null) ? 0 : data.size();
        int sheetNamesSize = (sheetNames == null) ? 0 : sheetNames.size();
        if (dataSize != sheetNamesSize) {
            throw new IllegalArgumentException("The number of data lists (" + dataSize
                    + ") must match the number of sheet names (" + sheetNamesSize + ").");
        }
        if (dataSize == 0) {
            log.warn("Export data for multi-sheet is empty, an empty Excel file will be generated. fileName: {}", fileName);
        }
        buildResponse(response, fileName, ExcelTypeEnum.XLSX);
        try (
                ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), head)
                        .excelType(ExcelTypeEnum.XLSX).build()) {
            for (int i = 0; i < dataSize; i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetNames.get(i)).build();
                excelWriter.write(data.get(i), writeSheet);
            }
        }
    }
    
    /**
     * 导出带自定义处理器的 Excel 文件。
     *
     * @throws IOException 历史签名保留
     */
    public static <T> void exportWithHandler(HttpServletResponse response, String fileName, String sheetName,
                                             List<T> list, Class<T> head, WriteHandler writeHandler) throws IOException {
        if (DataUtils.isEmpty(list)) {
            log.warn("Export data is empty for handler-based export, an empty Excel file with headers will be generated. fileName: {}", fileName);
        }
        Objects.requireNonNull(writeHandler, "WriteHandler cannot be null.");
        buildResponse(response, fileName, ExcelTypeEnum.XLSX);
        try (OutputStream os = response.getOutputStream()) {
            export(os, sheetName, list, head, ExcelTypeEnum.XLSX, writeHandler);
        }
    }
    
    // ==================== 读取 ====================
    
    /**
     * 从上传文件读取单 sheet 数据 (默认第一个 sheet)。
     * 全量入内存, 仅适合小文件。
     */
    public static <T> List<T> read(MultipartFile file, Class<T> clazz) {
        if (file == null || file.isEmpty()) {
            log.warn("Uploaded excel file is empty.");
            return Collections.emptyList();
        }
        try (InputStream inputStream = file.getInputStream()) {
            return EasyExcel.read(inputStream).head(clazz).sheet().doReadSync();
        } catch (IOException e) {
            log.error("Failed to read excel file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to read excel file", e);
        }
    }
    
    /**
     * 从上传文件读取指定 sheet (按 0-based 索引)。全量入内存。
     */
    public static <T> List<T> read(MultipartFile file, int sheetIndex, Class<T> clazz) {
        if (file == null || file.isEmpty()) {
            log.warn("Uploaded excel file is empty.");
            return Collections.emptyList();
        }
        try (InputStream inputStream = file.getInputStream()) {
            return EasyExcel.read(inputStream).head(clazz).sheet(sheetIndex).doReadSync();
        } catch (IOException e) {
            log.error("Failed to read sheet {} of {}: {}", sheetIndex, file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("Failed to read excel sheet", e);
        }
    }
    
    /**
     * 多 sheet 读取 (不同 sheet 可不同表头类)。
     *
     * @param headMap key=sheet 索引 (0-based), value=该 sheet 的表头类
     * @return key=sheet 索引, value=该 sheet 数据 List (无序, 按 map 顺序填充)
     */
    public static Map<Integer, List<?>> readAllSheets(MultipartFile file, Map<Integer, Class<?>> headMap) {
        Objects.requireNonNull(headMap, "headMap must not be null");
        if (headMap.isEmpty()) {
            return Collections.emptyMap();
        }
        if (file == null || file.isEmpty()) {
            log.warn("Uploaded excel file is empty.");
            return Collections.emptyMap();
        }
        Map<Integer, List<?>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Class<?>> e : headMap.entrySet()) {
            try (InputStream is = file.getInputStream()) {
                List<?> rows = EasyExcel.read(is).head(e.getValue()).sheet(e.getKey()).doReadSync();
                result.put(e.getKey(), rows);
            } catch (IOException ex) {
                log.error("Failed to read sheet {} of {}", e.getKey(), file.getOriginalFilename(), ex);
                throw new RuntimeException("Failed to read sheet " + e.getKey(), ex);
            }
        }
        return result;
    }
    
    /**
     * 带失败行收集的读取。解析异常 + 业务校验异常均记入 {@link ImportResult#failures},
     * 不中断后续行解析; 成功行进 {@link ImportResult#success}。
     * 全量入内存, 适合中小文件且需要「部分成功」语义的导入场景。
     *
     * @param validator 行级业务校验, 抛异常即记为失败; 传 null 则只收集解析异常
     */
    public static <T> ImportResult<T> read(MultipartFile file, Class<T> clazz, RowValidator<T> validator) {
        if (file == null || file.isEmpty()) {
            log.warn("Uploaded excel file is empty.");
            return new ImportResult<>(Collections.emptyList(), Collections.emptyList(), 0);
        }
        CollectingReadListener<T> listener = new CollectingReadListener<>(validator);
        try (InputStream is = file.getInputStream()) {
            EasyExcel.read(is, clazz, listener).sheet().doRead();
        } catch (IOException e) {
            log.error("Failed to read excel file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to read excel file", e);
        }
        return new ImportResult<>(listener.success, listener.failures, listener.total);
    }
    
    /**
     * 流式分批读取 (OOM-safe): 每累积 {@code batchSize} 行回调一次 {@code batchConsumer},
     * 适用于大数据导入 (如批量入库)。不收集失败行。
     *
     * @param batchSize 每批行数 (建议 500~2000)
     */
    public static <T> void read(MultipartFile file, Class<T> clazz, int batchSize,
                                Consumer<List<T>> batchConsumer) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be > 0");
        }
        if (file == null || file.isEmpty()) {
            log.warn("Uploaded excel file is empty.");
            return;
        }
        try (InputStream is = file.getInputStream()) {
            EasyExcel.read(is, clazz, new PageReadListener<>(batchConsumer, batchSize)).sheet().doRead();
        } catch (IOException e) {
            log.error("Failed to read excel file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to read excel file", e);
        }
    }
    
    // ==================== 模板填充 ====================
    
    /**
     * 模板填充: 单对象 + 列表数据同调 (典型: 表头填单值, 表格区填列表)。
     *
     * @param template   模板输入流 (调用方负责关闭; 此处不关闭)
     * @param out        生成文件输出流 (调用方负责关闭)
     * @param singleData 填入模板的单值 (Map 或 POJO), 可为 null
     * @param listData   填入模板的列表, 可为 null/empty
     * @param config     填充配置 (forceNewRow 等), 可为 null (使用默认)
     */
    public static void fill(InputStream template, OutputStream out, Object singleData, List<?> listData,
                            FillConfig config) {
        FillConfig cfg = config != null ? config : FillConfig.builder().forceNewRow(Boolean.TRUE).build();
        try (ExcelWriter writer = EasyExcel.write(out).withTemplate(template).autoCloseStream(false).build()) {
            WriteSheet sheet = EasyExcel.writerSheet().build();
            if (singleData != null) {
                writer.fill(singleData, cfg, sheet);
            }
            if (listData != null && !listData.isEmpty()) {
                writer.fill(listData, cfg, sheet);
            }
        }
    }
    
    /** {@link #fill} 默认配置版。 */
    public static void fill(InputStream template, OutputStream out, Object singleData, List<?> listData) {
        fill(template, out, singleData, listData, null);
    }
    
    /**
     * 模板填充单对象 (历史方法保留)。
     */
    public static void fillFromTemplate(InputStream templateStream, OutputStream outputStream, Object data) {
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(templateStream).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            excelWriter.fill(data, writeSheet);
        }
    }
    
    /**
     * 模板填充列表 (历史方法保留)。
     */
    public static void fillListFromTemplate(InputStream templateStream, OutputStream outputStream, List<?> listData,
                                            FillConfig fillConfig) {
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(templateStream).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            excelWriter.fill(listData, fillConfig, writeSheet);
        }
    }
    
    // ==================== 内部工具 ====================
    
    /**
     * 收集成功行 + 失败行的 ReadListener。继承 AbstractIgnoreExceptionReadListener
     * 以保证单行异常不中断整体解析。
     */
    private static final class CollectingReadListener<T> extends AbstractIgnoreExceptionReadListener<T> {
        
        private final List<T> success = new ArrayList<>();
        private final List<RowError> failures = new ArrayList<>();
        private final RowValidator<T> validator;
        private int total = 0;
        
        CollectingReadListener(RowValidator<T> validator) {
            this.validator = validator;
        }
        
        @Override
        public void invoke(T row, AnalysisContext context) {
            int rowIndex = context.readRowHolder().getRowIndex();
            total++;
            if (validator == null) {
                success.add(row);
                return;
            }
            try {
                validator.validate(rowIndex, row);
                success.add(row);
            } catch (Exception e) {
                failures.add(new RowError(rowIndex, String.valueOf(row), e.getMessage()));
            }
        }
        
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // no-op
        }
        
        @Override
        public void onException(Exception e, AnalysisContext context) {
            int rowIndex = context.readRowHolder().getRowIndex();
            failures.add(new RowError(rowIndex, "parse error", e.getMessage()));
        }
    }
}
