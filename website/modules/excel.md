# Excel `spring-boot-nebula-excel`

基于 [FastExcel](https://gitee.com/draco1118/fastexcel)（EasyExcel 活跃分支）的 Excel 导入导出工具类封装。
覆盖注解/动态表头导出、流式读写、多 sheet、CSV、模板填充、样式预设与导入失败行收集。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-excel</artifactId>
    <version>3.0.3</version>
</dependency>
```

## 主要能力

| 类 | 说明 |
|----|------|
| `ExcelUtils` | 读写门面，全部静态方法 |
| `ImportResult<T>` | 导入结果：`success` + `failures` + `totalRows` |
| `RowError` | 失败行：`rowIndex` + `rowData` + `errorMessage` |
| `RowValidator<T>` | 行级业务校验函数式接口 |
| `StylePresets` | 常用写入样式/策略工厂（冻结表头、列宽、合并） |

> **约定**：所有以 `OutputStream` 为 sink 的方法均设置 `autoCloseStream=false`，**调用方负责关闭流**；HTTP 响应式方法内部已自行关闭 response 输出流。写入/读取失败统一抛 `RuntimeException` 并带业务上下文日志，不抛 checked `IOException`。

## 导出

### HTTP 响应导出（XLSX / CSV）

```java
@GetMapping("excel/export")
public void export(HttpServletResponse response) {
    ExcelUtils.export(response, "测试导出", dataList, YourVO.class);
}

@GetMapping("excel/export-csv")
public void exportCsv(HttpServletResponse response) {
    ExcelUtils.exportCsv(response, "测试导出", "sheet1", dataList, YourVO.class);
}

@GetMapping("excel/export-with-date-suffix")
public void exportWithDateSuffix(HttpServletResponse response) {
    ExcelUtils.exportWithDateSuffix(response, "测试导出", dataList, YourVO.class);
}
```

### 导出到 File / byte[] / OutputStream

解耦 HTTP，便于异步任务、OSS 上传、邮件附件。

```java
// 写文件
File f = ExcelUtils.exportToFile(new File("/tmp/r.xlsx"), "sheet1", list, YourVO.class);

// 字节数组 (内存缓冲, 中小数据量)
byte[] bytes = ExcelUtils.exportToBytes("sheet1", list, YourVO.class, ExcelTypeEnum.XLSX);

// 任意 OutputStream (调用方负责关闭流)
try (OutputStream os = new FileOutputStream("/tmp/r.xlsx")) {
    ExcelUtils.export(os, "sheet1", list, YourVO.class, ExcelTypeEnum.XLSX);
}
```

### 动态表头导出

运行时构造列名，无需注解类，适合自定义报表。

```java
List<List<String>> head = List.of(List.of("姓名"), List.of("年龄"));
List<List<Object>> data = List.of(List.of("张三", 20), List.of("李四", 30));
ExcelUtils.export(response, "动态表", "sheet1", head, data);
```

### 流式导出（Iterator，OOM-safe）

数据源为迭代器/游标，避免全量入内存。

```java
Iterator<YourVO> it = loadFromDbCursor();
try (OutputStream os = new FileOutputStream("/tmp/big.xlsx")) {
    ExcelUtils.export(os, "sheet1", it, YourVO.class, 1000 /* 每批行数 */);
}
```

### 大数据 auto-split 多 Sheet

超 `maxRowsPerSheet` 自动切下一 sheet，规避单 sheet 行上限（XLSX 上限 1,048,576）。

```java
try (OutputStream os = new FileOutputStream("/tmp/huge.xlsx")) {
    ExcelUtils.exportAutoSplit(os, "sh", dataIterator(), YourVO.class,
        100_000 /* maxRowsPerSheet */, 1000 /* batchSize */, ExcelTypeEnum.XLSX);
}
```

### 多 Sheet 导出（共享表头）

```java
ExcelUtils.exportMultiSheet(response, "multi",
    List.of("sheet1", "sheet2"), List.of(list1, list2), YourVO.class);
```

## 读取

### 简单读取（默认第一个 sheet）

```java
List<YourVO> list = ExcelUtils.read(multipartFile, YourVO.class);
```

### 按 sheet 索引读取

```java
List<YourVO> sheet2 = ExcelUtils.read(multipartFile, 1, YourVO.class);
```

### 多 Sheet 不同表头

```java
Map<Integer, Class<?>> headMap = new LinkedHashMap<>();
headMap.put(0, UserVO.class);
headMap.put(1, OrderVO.class);
Map<Integer, List<?>> sheets = ExcelUtils.readAllSheets(multipartFile, headMap);
```

### 导入失败行收集（部分成功语义）

解析异常与业务校验异常均记入 `failures`，不中断后续行；成功行进 `success`。

```java
ImportResult<YourVO> result = ExcelUtils.read(multipartFile, YourVO.class, (rowIndex, row) -> {
    if (row.getAge() < 0) {
        throw new IllegalStateException("age must >= 0");
    }
});
result.success();   // List<YourVO> 通过校验的行
result.failures();  // List<RowError> 失败行 (行号+原因)
result.totalRows(); // 总数据行数
```

### 流式分批读取（OOM-safe，批量入库场景）

```java
ExcelUtils.read(multipartFile, YourVO.class, 1000 /* batchSize */, batch -> {
    saveBatch(batch); // 每累积 batchSize 行回调一次, 处理完即丢弃
});
```

## 模板填充

单对象（表头区）+ 列表（表格区）一次完成。占位符语法 `{field}`。

```java
try (InputStream tpl = new FileInputStream("template.xlsx");
     OutputStream os = new FileOutputStream("out.xlsx")) {
    ExcelUtils.fill(tpl, os, headerData /* {name}/{age} */, tableList, null /* 默认 FillConfig */);
}
```

## 样式预设

传入 `WriteHandler...` 参数，组合使用。

```java
ExcelUtils.export(response, "r", "sheet1", list, YourVO.class);
// 等价于带样式:
ExcelUtils.exportToFile(file, "sheet1", list, YourVO.class,
    ExcelTypeEnum.XLSX,
    StylePresets.freezeHeader(),   // 冻结首行
    StylePresets.autoWidth());     // 列宽自适应
```

| 工厂 | 说明 |
|------|------|
| `freezeHeader()` / `freezePane(col,row)` | 冻结表头 / 通用冻结窗格 |
| `autoWidth()` | 列宽按内容最长匹配（大数据量慎用） |
| `fixedWidth(width)` | 固定列宽 |
| `mergeOnce(r1,r2,c1,c2)` | 绝对区域合并 |
| `mergeLoop(eachRows,colIdx)` | 循环块合并（每 N 行合并） |

## 示例模块

`spring-boot-nebula-excel-sample`。
