# Excel `spring-boot-nebula-excel`

Excel import/export utility built on [FastExcel](https://gitee.com/draco1118/fastexcel) (the actively maintained EasyExcel fork).
Covers annotation-based and dynamic-header export, streaming read/write, multi-sheet, CSV, template fill, style presets, and failed-row collection on import.

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-excel</artifactId>
    <version>3.0.6</version>
</dependency>
```

## Main Capabilities

| Class | Description |
|-------|-------------|
| `ExcelUtils` | Read/write facade, all static methods |
| `ImportResult<T>` | Import result: `success` + `failures` + `totalRows` |
| `RowError` | Failed row: `rowIndex` + `rowData` + `errorMessage` |
| `RowValidator<T>` | Functional interface for row-level business validation |
| `StylePresets` | Factory for common write styles/strategies (frozen header, column width, merging) |

> **Convention**: all methods that sink to an `OutputStream` set `autoCloseStream=false` — **the caller is responsible for closing the stream**; HTTP-response methods close the response output stream internally. Write/read failures uniformly throw `RuntimeException` with business-context logging, never checked `IOException`.

## Export

### HTTP Response Export (XLSX / CSV)

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

### Export to File / byte[] / OutputStream

Decoupled from HTTP — handy for async jobs, OSS uploads, and email attachments.

```java
// Write to file
File f = ExcelUtils.exportToFile(new File("/tmp/r.xlsx"), "sheet1", list, YourVO.class);

// Byte array (in-memory buffer, for small-to-medium data volumes)
byte[] bytes = ExcelUtils.exportToBytes("sheet1", list, YourVO.class, ExcelTypeEnum.XLSX);

// Any OutputStream (caller closes the stream)
try (OutputStream os = new FileOutputStream("/tmp/r.xlsx")) {
    ExcelUtils.export(os, "sheet1", list, YourVO.class, ExcelTypeEnum.XLSX);
}
```

### Dynamic Header Export

Construct column names at runtime without annotated classes — ideal for custom reports.

```java
List<List<String>> head = List.of(List.of("姓名"), List.of("年龄"));
List<List<Object>> data = List.of(List.of("张三", 20), List.of("李四", 30));
ExcelUtils.export(response, "动态表", "sheet1", head, data);
```

### Streaming Export (Iterator, OOM-safe)

The data source is an iterator/cursor, avoiding loading everything into memory.

```java
Iterator<YourVO> it = loadFromDbCursor();
try (OutputStream os = new FileOutputStream("/tmp/big.xlsx")) {
    ExcelUtils.export(os, "sheet1", it, YourVO.class, 1000 /* rows per batch */);
}
```

### Large-Volume auto-split Multi-Sheet

Automatically rolls to the next sheet beyond `maxRowsPerSheet`, avoiding the per-sheet row limit (XLSX caps at 1,048,576).

```java
try (OutputStream os = new FileOutputStream("/tmp/huge.xlsx")) {
    ExcelUtils.exportAutoSplit(os, "sh", dataIterator(), YourVO.class,
        100_000 /* maxRowsPerSheet */, 1000 /* batchSize */, ExcelTypeEnum.XLSX);
}
```

### Multi-Sheet Export (Shared Header)

```java
ExcelUtils.exportMultiSheet(response, "multi",
    List.of("sheet1", "sheet2"), List.of(list1, list2), YourVO.class);
```

## Import

### Simple Read (First Sheet by Default)

```java
List<YourVO> list = ExcelUtils.read(multipartFile, YourVO.class);
```

### Read by Sheet Index

```java
List<YourVO> sheet2 = ExcelUtils.read(multipartFile, 1, YourVO.class);
```

### Multiple Sheets with Different Headers

```java
Map<Integer, Class<?>> headMap = new LinkedHashMap<>();
headMap.put(0, UserVO.class);
headMap.put(1, OrderVO.class);
Map<Integer, List<?>> sheets = ExcelUtils.readAllSheets(multipartFile, headMap);
```

### Failed-Row Collection on Import (Partial-Success Semantics)

Both parsing exceptions and business validation exceptions are recorded in `failures` without interrupting subsequent rows; passing rows go into `success`.

```java
ImportResult<YourVO> result = ExcelUtils.read(multipartFile, YourVO.class, (rowIndex, row) -> {
    if (row.getAge() < 0) {
        throw new IllegalStateException("age must >= 0");
    }
});
result.success();   // List<YourVO> rows that passed validation
result.failures();  // List<RowError> failed rows (row index + reason)
result.totalRows(); // Total data row count
```

### Streaming Batched Read (OOM-safe, for Bulk Persistence)

```java
ExcelUtils.read(multipartFile, YourVO.class, 1000 /* batchSize */, batch -> {
    saveBatch(batch); // Called once per batchSize rows accumulated; discarded after processing
});
```

## Template Fill

Single object (header area) + list (table area) in one pass. Placeholder syntax is `{field}`.

```java
try (InputStream tpl = new FileInputStream("template.xlsx");
     OutputStream os = new FileOutputStream("out.xlsx")) {
    ExcelUtils.fill(tpl, os, headerData /* {name}/{age} */, tableList, null /* default FillConfig */);
}
```

## Style Presets

Pass `WriteHandler...` arguments, combinable.

```java
ExcelUtils.export(response, "r", "sheet1", list, YourVO.class);
// Equivalent with styles:
ExcelUtils.exportToFile(file, "sheet1", list, YourVO.class,
    ExcelTypeEnum.XLSX,
    StylePresets.freezeHeader(),   // Freeze first row
    StylePresets.autoWidth());     // Auto-fit column width
```

| Factory | Description |
|---------|-------------|
| `freezeHeader()` / `freezePane(col,row)` | Freeze header / generic freeze pane |
| `autoWidth()` | Column width fits the longest content (use cautiously with large data) |
| `fixedWidth(width)` | Fixed column width |
| `mergeOnce(r1,r2,c1,c2)` | Absolute-region merge |
| `mergeLoop(eachRows,colIdx)` | Loop-block merge (merge every N rows) |

## Sample Module

`spring-boot-nebula-excel-sample`.
