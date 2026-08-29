# 快速开始

## 方式一：脚手架一键生成（推荐）

适合新服务。**在想放置项目的父目录执行**（如 `~/projects`），生成器会在当前目录创建以 `artifactId` 命名的新文件夹：

```bash
mvn archetype:generate \
    -DarchetypeGroupId=io.github.weihubeats \
    -DarchetypeArtifactId=spring-boot-nebula-archetype \
    -DarchetypeVersion=3.0.6 \
    -DgroupId=com.example \
    -DartifactId=demo \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackage=com.example.demo \
    -DinteractiveMode=false
```

生成后即可运行：

```bash
cd demo && mvn verify && mvn spring-boot:run -pl demo-start
```

详见 [项目脚手架](/modules/archetype)。

## 方式二：手动引入

### 1. 引入 BOM（推荐）

在父 POM 中统一版本，避免各项目依赖版本不一致（如 Redisson 3.14 vs 3.61 导致行为差异）：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.weihubeats</groupId>
            <artifactId>spring-boot-nebula-dependencies</artifactId>
            <version>3.0.6</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 引入 Web 模块

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web</artifactId>
</dependency>
```

### 3. 编写启动类

```java
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(WebApplication.class, args);
    }
}
```

### 4. 编写接口

无需手动包装 `Response`，在 Controller 方法上添加 `@NebulaResponseBody` 即可（也支持标注在类上，对全部方法生效）：

```java
@GetMapping("/test")
@NebulaResponseBody
public String test() {
    return "小奏";
}
```

响应格式：

```json
{
  "code": 200,
  "data": "小奏",
  "msg": "success"
}
```

### 5. 按需引入更多模块

| 能力 | ArtifactId | 文档 |
|------|------------|------|
| MyBatis-Plus 封装 | `spring-boot-nebula-mybatis` | [查看](/modules/mybatis) |
| 读写分离 | `spring-boot-nebula-dynamic-datasource` | [查看](/modules/dynamic-datasource) |
| 分布式锁 | `spring-boot-nebula-distribute-lock` | [查看](/modules/distribute-lock) |
| Excel 导入导出 | `spring-boot-nebula-excel` | [查看](/modules/excel) |
| 区域路由 JOIN | `spring-boot-nebula-join` | [查看](/modules/join) |
| Feign 自动解包 | `spring-boot-nebula-feign` | [查看](/modules/feign) |
| 日志脱敏与告警 | `spring-boot-nebula-logback` | [查看](/modules/log) |
| DDD 聚合根 | `spring-boot-nebula-aggregate` | [查看](/modules/aggregate) |

## 本地运行示例

每个能力在 `spring-boot-nebula-samples` 下都有可运行的最小示例：

```bash
cd spring-boot-nebula-samples/spring-boot-nebula-web-sample
mvn spring-boot:run
```