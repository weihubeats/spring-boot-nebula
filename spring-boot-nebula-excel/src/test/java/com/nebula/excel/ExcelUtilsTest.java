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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.support.ExcelTypeEnum;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/**
 * ExcelUtils round-trip 测试: 用真实 fastexcel 读写验证各功能行为。
 */
class ExcelUtilsTest {
    
    @TempDir
    File tempDir;
    
    public static class Person {
        
        @ExcelProperty(value = "name", index = 0)
        private String name;
        @ExcelProperty(value = "age", index = 1)
        private int age;
        
        public Person() {
        }
        
        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
    }
    
    private static List<Person> persons(String... names) {
        List<Person> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            list.add(new Person(names[i], i));
        }
        return list;
    }
    
    private static MockMultipartFile multipart(File f, String name) throws Exception {
        byte[] bytes = Files.readAllBytes(f.toPath());
        return new MockMultipartFile(name, name, "application/octet-stream", bytes);
    }
    
    // ==================== 导出 ====================
    
    @Test
    void exportToFile_roundTrip_xlsx() throws Exception {
        List<Person> data = persons("Alice", "Bob", "Carol");
        File f = new File(tempDir, "p.xlsx");
        ExcelUtils.exportToFile(f, "s1", data, Person.class, ExcelTypeEnum.XLSX);
        
        assertTrue(f.length() > 0);
        List<Person> back = ExcelUtils.read(multipart(f, "p.xlsx"), Person.class);
        assertEquals(data.size(), back.size());
        assertEquals("Alice", back.get(0).getName());
        assertEquals(0, back.get(0).getAge());
    }
    
    @Test
    void exportToBytes_nonEmpty() {
        byte[] bytes = ExcelUtils.exportToBytes("s", persons("X", "Y"), Person.class, ExcelTypeEnum.XLSX);
        assertTrue(bytes.length > 0);
    }
    
    @Test
    void exportCsv_textContentHasHeader() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelUtils.export(out, "s", persons("Alice", "Bob"), Person.class, ExcelTypeEnum.CSV);
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("name"));
        assertTrue(text.contains("Alice"));
        assertTrue(text.contains("Bob"));
    }
    
    @Test
    void exportDynamicHead_readBackAsMaps() throws Exception {
        List<List<String>> head = List.of(List.of("col1"), List.of("col2"));
        List<List<Object>> data = List.of(List.of("a", 1), List.of("b", 2));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelUtils.export(out, "s", head, data, ExcelTypeEnum.XLSX);
        
        List<Map<Integer, String>> rows;
        try (InputStream is = new ByteArrayInputStream(out.toByteArray())) {
            rows = EasyExcel.read(is).sheet().doReadSync();
        }
        assertEquals(2, rows.size());
        assertEquals("a", rows.get(0).get(0));
        assertEquals("1", rows.get(0).get(1));
        assertEquals("b", rows.get(1).get(0));
    }
    
    @Test
    void exportStreamIterator_readBack() throws Exception {
        List<Person> src = persons("A", "B", "C", "D", "E");
        Iterator<Person> it = src.iterator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelUtils.export(out, "s", it, Person.class, 2, ExcelTypeEnum.XLSX);
        
        List<Person> back = EasyExcel.read(new ByteArrayInputStream(out.toByteArray())).head(Person.class).sheet().doReadSync();
        assertEquals(src.size(), back.size());
        assertEquals("A", back.get(0).getName());
        assertEquals("E", back.get(4).getName());
    }
    
    @Test
    void exportAutoSplit_multipleSheets() throws Exception {
        List<Person> src = persons("A", "B", "C", "D", "E");
        File f = new File(tempDir, "split.xlsx");
        try (var out = new java.io.FileOutputStream(f)) {
            ExcelUtils.exportAutoSplit(out, "sh", src.iterator(), Person.class, 2, 1, ExcelTypeEnum.XLSX);
        }
        
        Map<Integer, Class<?>> headMap = new LinkedHashMap<>();
        headMap.put(0, Person.class);
        headMap.put(1, Person.class);
        headMap.put(2, Person.class);
        Map<Integer, List<?>> sheets = ExcelUtils.readAllSheets(multipart(f, "split.xlsx"), headMap);
        
        assertEquals(2, sheets.get(0).size());
        assertEquals(2, sheets.get(1).size());
        assertEquals(1, sheets.get(2).size());
    }
    
    @Test
    void exportAutoSplit_batchSizeTooLarge_throws() {
        assertThrows(IllegalArgumentException.class, () -> ExcelUtils.exportAutoSplit(
                new ByteArrayOutputStream(), "sh", persons("A").iterator(), Person.class, 2, 5, ExcelTypeEnum.XLSX));
    }
    
    @Test
    void exportWithStylePresets_noThrow() {
        byte[] bytes = ExcelUtils.exportToBytes("s", persons("A", "B"), Person.class,
                ExcelTypeEnum.XLSX, StylePresets.freezeHeader(), StylePresets.autoWidth());
        assertTrue(bytes.length > 0);
    }
    
    // ==================== 读取 ====================
    
    @Test
    void readWithValidator_collectsSuccessAndFailures() throws Exception {
        List<Person> src = persons("Alice", "Bob", "Carol");
        src.get(1).setAge(-1); // Bob 校验失败
        File f = new File(tempDir, "v.xlsx");
        ExcelUtils.exportToFile(f, "s", src, Person.class);
        
        ImportResult<Person> result = ExcelUtils.read(multipart(f, "v.xlsx"), Person.class,
                (rowIndex, row) -> {
                    if (row.getAge() < 0) {
                        throw new IllegalStateException("age must >= 0");
                    }
                });
        
        assertEquals(3, result.totalRows());
        assertEquals(2, result.success().size());
        assertEquals(1, result.failures().size());
        // Bob 校验失败, success = [Alice, Carol]
        assertEquals("Carol", result.success().get(1).getName());
        assertTrue(result.failures().get(0).errorMessage().contains("age must >= 0"));
    }
    
    @Test
    void readBatchConsumer_streamingFlush() throws Exception {
        List<Person> src = persons("A", "B", "C", "D", "E");
        File f = new File(tempDir, "b.xlsx");
        ExcelUtils.exportToFile(f, "s", src, Person.class);
        
        List<Person> collected = new ArrayList<>();
        List<Integer> batchSizes = new ArrayList<>();
        ExcelUtils.read(multipart(f, "b.xlsx"), Person.class, 2, batch -> {
            batchSizes.add(batch.size());
            collected.addAll(batch);
        });
        
        assertEquals(5, collected.size());
        // 3 批: 2, 2, 1
        assertEquals(List.of(2, 2, 1), batchSizes);
    }
    
    @Test
    void readSheetByIndex() throws Exception {
        List<Person> src = persons("A", "B", "C");
        File f = new File(tempDir, "multi.xlsx");
        try (
                var out = new java.io.FileOutputStream(f);
                var writer = cn.idev.excel.EasyExcel.write(out, Person.class).build()) {
            writer.write(src, cn.idev.excel.EasyExcel.writerSheet(0, "first").build());
            writer.write(persons("X", "Y"), cn.idev.excel.EasyExcel.writerSheet(1, "second").build());
        }
        
        List<Person> sheet1 = ExcelUtils.read(multipart(f, "multi.xlsx"), 1, Person.class);
        assertEquals(2, sheet1.size());
        assertEquals("X", sheet1.get(0).getName());
    }
    
    @Test
    void readAllSheets_differentHeads() throws Exception {
        // 复用 auto-split 产物结构: 3 sheet 同 head
        List<Person> src = persons("A", "B", "C", "D", "E");
        File f = new File(tempDir, "all.xlsx");
        try (var out = new java.io.FileOutputStream(f)) {
            ExcelUtils.exportAutoSplit(out, "sh", src.iterator(), Person.class, 2, 1, ExcelTypeEnum.XLSX);
        }
        Map<Integer, Class<?>> headMap = new LinkedHashMap<>();
        headMap.put(0, Person.class);
        headMap.put(2, Person.class);
        Map<Integer, List<?>> sheets = ExcelUtils.readAllSheets(multipart(f, "all.xlsx"), headMap);
        
        assertEquals(2, sheets.size());
        assertEquals(2, sheets.get(0).size());
        assertEquals(1, sheets.get(2).size());
    }
    
    @Test
    void readEmptyFile_returnsEmpty() {
        MockMultipartFile empty = new MockMultipartFile("empty", new byte[0]);
        assertTrue(ExcelUtils.read(empty, Person.class).isEmpty());
        ImportResult<Person> r = ExcelUtils.read(empty, Person.class, null);
        assertEquals(0, r.totalRows());
    }
    
    @Test
    void readBatchConsumer_negativeBatch_throws() {
        MockMultipartFile empty = new MockMultipartFile("e", new byte[0]);
        assertThrows(IllegalArgumentException.class,
                () -> ExcelUtils.read(empty, Person.class, -1, b -> {
                }));
    }
    
    // ==================== 模板填充 ====================
    
    @Test
    void fill_singleObject_replacesPlaceholder() throws Exception {
        // 构造模板: 表头行含 {name} {age} 占位符
        List<List<String>> head = List.of(List.of("{name}"), List.of("{age}"));
        ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
        ExcelUtils.export(templateOut, "s", head, List.of(), ExcelTypeEnum.XLSX);
        byte[] template = templateOut.toByteArray();
        
        Person p = new Person("Alice", 30);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ExcelUtils.fill(new ByteArrayInputStream(template), result, p, null, null);
        
        assertTrue(result.size() > 0);
        List<Map<Integer, String>> rows;
        try (InputStream is = new ByteArrayInputStream(result.toByteArray())) {
            rows = EasyExcel.read(is).headRowNumber(0).sheet().doReadSync();
        }
        assertFalse(rows.isEmpty());
        assertEquals("Alice", rows.get(0).get(0));
        assertEquals("30", rows.get(0).get(1));
    }
    
    @Test
    void fill_emptyInputs_noThrow() throws Exception {
        List<List<String>> head = List.of(List.of("{name}"));
        ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
        ExcelUtils.export(templateOut, "s", head, List.of(), ExcelTypeEnum.XLSX);
        
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ExcelUtils.fill(new ByteArrayInputStream(templateOut.toByteArray()), result, null, null);
        assertTrue(result.size() > 0);
    }
    
    // ==================== StylePresets ====================
    
    @Test
    void stylePresets_factoriesReturnNonNull() {
        assertNotNull(StylePresets.freezeHeader());
        assertNotNull(StylePresets.freezePane(1, 1));
        assertNotNull(StylePresets.autoWidth());
        assertNotNull(StylePresets.fixedWidth(20));
        assertNotNull(StylePresets.mergeOnce(0, 0, 0, 1));
        assertNotNull(StylePresets.mergeLoop(2, 0));
    }
    
    @Test
    void convert2FileName_withType() {
        assertEquals("r.csv", ExcelUtils.convert2FileName("r", ExcelTypeEnum.CSV));
        assertEquals("r.xlsx", ExcelUtils.convert2FileName("r"));
    }
}
