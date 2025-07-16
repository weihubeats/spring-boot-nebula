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
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.handler.WriteHandler;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.fill.FillConfig;
import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.TimeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : wh
 * @date : 2025/6/24
 * @description:
 */
@Slf4j
public class ExcelUtils {
    
    /**
     * 为 HTTP 响应设置 Excel 导出的标准 Header。
     *
     * @param response HttpServletResponse 对象
     * @param fileName 导出的文件名 (会自动添加 .xlsx 后缀)
     * @throws IOException 当编码不支持时抛出
     */
    public static void buildResponse(HttpServletResponse response, String fileName) {
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ExcelTypeEnum.XLSX.getValue());
    }
    
    /**
     * 添加Excel文件后缀
     *
     * @param fileNameCode 文件名
     * @return
     */
    public static String convert2FileName(String fileNameCode) {
        return fileNameCode + ExcelTypeEnum.XLSX.getValue();
    }
    
    /**
     * 导出单个 Sheet 的 Excel 文件。
     * 如果数据列表为空，则导出一个只包含表头的空 Excel 文件。
     *
     * @param response  HttpServletResponse 对象
     * @param fileName  文件名 (不含后缀)
     * @param sheetName Sheet 名称
     * @param list      要导出的数据列表
     * @param head      Excel 表头类
     * @param <T>       数据的泛型
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> list,
                                  Class<T> head) {
        buildResponse(response, fileName);
        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os, head).sheet(sheetName).doWrite(list);
        } catch (IOException e) {
            log.error("Excel export failed, fileName: {}, sheetName: {}", fileName, sheetName, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    /**
     * 导出单个 Sheet 的 Excel 文件，使用文件名作为 Sheet 名。
     * 运行null或空的列表将导出一个只包含表头的空 Excel 文件。
     *
     * @param response HttpServletResponse 对象
     * @param fileName 文件名 (不含后缀), 也将作为 Sheet 名
     * @param list     要导出的数据列表
     * @param head     Excel 表头类
     * @param <T>      数据的泛型
     */
    public static <T> void export(HttpServletResponse response, String fileName, List<T> list,
                                  Class<T> head) {
        export(response, fileName, fileName, list, head);
    }
    
    public static <T> String exportWithDateSuffix(HttpServletResponse response, String fileName,
                                                  List<T> list, Class<T> head) {
        return exportWithDateSuffix(response, fileName, fileName, list, head);
    }
    
    /**
     * 导出单个 Sheet 的 Excel 文件，并自动在文件名后附加当前日期时间。
     *
     * @param response  HttpServletResponse 对象
     * @param fileName  文件名 (不含后缀)
     * @param sheetName Sheet 名称
     * @param list      要导出的数据列表
     * @param head      Excel 表头类
     * @param <T>       数据的泛型
     * @return 附加了日期时间戳的完整文件名
     */
    public static <T> String exportWithDateSuffix(HttpServletResponse response, String fileName, String sheetName,
                                                  List<T> list, Class<T> head) {
        String dateTimeSuffix = TimeUtil.formatCurrentDateTime(TimeUtil.YYYYMMDDHHMMSS);
        String fullFileName = String.join("-", fileName, dateTimeSuffix);
        export(response, fullFileName, sheetName, list, head);
        
        return fullFileName;
    }
    
    /**
     * 导出包含多个 Sheet 的 Excel 文件。
     * 如果数据列表为空，则导出一个空的 Excel 文件。
     * 如果某个 sheet 的数据为空，则该 sheet 只会导出表头。
     *
     * @param response   HttpServletResponse 对象
     * @param fileName   文件名 (不含后缀)
     * @param sheetNames 多个 Sheet 的名称列表 (数量必须与 data 列表一致)
     * @param data       多个数据集的列表 (数量必须与 sheetNames 列表一致)
     * @param head       每个 Sheet 的表头类
     * @param <T>        数据的泛型
     * @throws IOException 写入响应流时发生 I/O 错误
     */
    public static <T> void exportMultiSheet(HttpServletResponse response, String fileName, List<String> sheetNames,
                                            List<List<T>> data, Class<T> head) throws IOException {
        int dataSize = (data == null) ? 0 : data.size();
        int sheetNamesSize = (sheetNames == null) ? 0 : sheetNames.size();
        
        if (dataSize != sheetNamesSize) {
            throw new IllegalArgumentException("The number of data lists (" + dataSize + ") must match the number of sheet names (" + sheetNamesSize + ").");
        }
        
        if (dataSize == 0) {
            log.warn("Export data for multi-sheet is empty, an empty Excel file will be generated. fileName: {}", fileName);
        }
        
        buildResponse(response, fileName);
        
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), head).build()) {
            for (int i = 0; i < dataSize; i++) {
                // 总是创建sheet。EasyExcel 会为空或null的列表仅写入表头。
                WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetNames.get(i)).build();
                excelWriter.write(data.get(i), writeSheet);
            }
            // 如果 dataSize 为 0, 循环不会执行，将创建一个空的Excel文件。
        }
    }
    
    /**
     * 导出带自定义处理器 (例如，添加批注) 的 Excel 文件。
     *
     * @param response     HttpServletResponse 对象
     * @param fileName     文件名 (不含后缀)
     * @param sheetName    Sheet 名称
     * @param list         要导出的数据列表
     * @param head         表头类
     * @param writeHandler 自定义处理器
     * @param <T>          数据的泛型
     * @throws IOException 写入响应流时发生 I/O 错误
     */
    public static <T> void exportWithHandler(HttpServletResponse response, String fileName, String sheetName,
                                             List<T> list, Class<T> head, WriteHandler writeHandler) throws IOException {
        if (DataUtils.isEmpty(list)) {
            log.warn("Export data is empty for handler-based export, an empty Excel file with headers will be generated. fileName: {}", fileName);
        }
        Objects.requireNonNull(writeHandler, "WriteHandler cannot be null.");
        
        buildResponse(response, fileName);
        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os, head)
                    .registerWriteHandler(writeHandler)
                    .sheet(sheetName)
                    .doWrite(list);
        }
    }
    
    /**
     * 从上传的文件中读取 Excel 数据。
     *
     * @param file  上传的 MultipartFile 文件
     * @param clazz 数据映射的类
     * @param <T>   数据的泛型
     * @return 读取到的数据列表，如果读取失败或文件为空则返回 null
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
     * 根据模板填充数据并生成 Excel 文件。
     *
     * @param templateStream 模板文件的输入流
     * @param outputStream   生成文件的输出流
     * @param data           要填充的数据对象 (可以是 Map 或自定义对象)
     */
    public static void fillFromTemplate(InputStream templateStream, OutputStream outputStream,
                                        Object data) {
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(templateStream).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            excelWriter.fill(data, writeSheet);
        }
    }
    
    /**
     * 根据模板填充列表数据并生成 Excel 文件。
     *
     * @param templateStream 模板文件的输入流
     * @param outputStream   生成文件的输出流
     * @param listData       要填充的列表数据
     * @param fillConfig     填充配置，例如是否强制换行
     */
    public static void fillListFromTemplate(InputStream templateStream, OutputStream outputStream, List<?> listData,
                                            FillConfig fillConfig) {
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(templateStream).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            excelWriter.fill(listData, fillConfig, writeSheet);
        }
    }
}
