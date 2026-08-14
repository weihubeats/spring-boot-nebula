# Excel `spring-boot-nebula-excel`

基于 EasyExcel 的 Excel 导入导出工具类封装。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-excel</artifactId>
    <version>3.0.3</version>
</dependency>
```

## 导出示例

```java
@GetMapping("excel/export")
public void exportExcel(HttpServletResponse response) {
    ExcelUtils.export(response, "测试导出", dataList, XiaoZouVO.class);
}

@GetMapping("excel/export-with-date-suffix")
public void exportWithDateSuffix(HttpServletResponse response) {
    ExcelUtils.exportWithDateSuffix(response, "测试导出", dataList, XiaoZouVO.class);
}
```

`ExcelUtils` 同时支持多 Sheet 导出、模板填充、同步读取等。

示例模块：`spring-boot-nebula-excel-sample`。