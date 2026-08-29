# Web 封装 `spring-boot-nebula-web`

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web</artifactId>
    <version>3.0.3</version>
</dependency>
```

## 统一响应 `@NebulaResponseBody`

Controller 直接返回业务对象，由框架自动包装为统一 JSON：

```java
@GetMapping("/test")
@NebulaResponseBody
public String test() {
    return "小奏";
}
```

**支持标注在类上**，对该 Controller 全部方法生效，无需逐个方法添加：

```java
@NebulaResponseBody
@RestController
@RequestMapping("/users")
public class UserController { ... }
```

规则：

| 场景 | 行为 |
|------|------|
| 方法与类同时标注 | 方法优先（如方法级指定了不同的 `objectMapper`） |
| 返回值已是 `NebulaResponse` | 不重复包装 |
| 自定义序列化 | `objectMapper()` 属性指定 `ObjectMapper` 子类，类/方法均可配置 |

## Feign / RPC 调用

推荐引入 [spring-boot-nebula-feign](/modules/feign)：Feign 方法直接声明业务返回类型，框架自动将 `NebulaResponse` 解包。

未引入该模块时，也可手动用 `NebulaResponse` 接收，并通过 `data()` 取数（会校验状态码）。

## 响应码自定义

内部错误码仍为 `int`，仅影响对外 JSON 中的 `code` 字段：

```yaml
# 成功码写成字符串（兼容旧项目）
nebula.web.response-code: Success

# 成功/失败统一映射（推荐）
nebula.web.code-mapping:
  200: Success
  400: Failure
  500: Error
```

未配置时默认返回数字，例如 `"code": 200`。

## 分页对象

- 查询参数继承 `NebulaPageQuery`
- 返回结果使用 `NebulaPageRes`

```java
@Data
public class StudentDTO extends NebulaPageQuery {
    private Long id;
    private String name;
    private Integer age;
}

@GetMapping("/list")
@NebulaResponseBody
public NebulaPageRes<StudentVO> list(StudentDTO studentDTO) {
    return studentService.list(studentDTO);
}
```

## 统一异常处理

参考 `NebulaRestExceptionHandler`，对常见异常自动封装为统一响应格式。

开启飞书异常告警：

```yaml
nebula:
  web:
    monitor:
      open: true
      type: feishu
      url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      limit:
        enabled: true        # 是否开启告警频率限制
        window-seconds: 60   # 限流窗口（秒）
        max-count: 3         # 窗口内同 key 最大告警次数
        storage: local       # local=单实例内存限流；redis=多实例共享限流（需配置 RedissonClient）
        key-prefix: nebula:alert:rate:   # storage=redis 时使用
        expire-seconds: 120  # storage=redis 时使用，需 ≥ window-seconds
```

自定义告警实现 `NebulaErrorMonitor` 接口即可替换默认行为；新增渠道实现 `NebulaAlertChannel` 接口并在 `monitor.type` 下装配。

## 时间戳参数 `@GetTimestamp`

`@GetTimestamp` 自动将时间戳参数转为 `LocalDateTime`。

## LocalDateTime 处理

模块通过全局 `JacksonTimeModule` 统一格式化 `LocalDateTime`，JSON body 字段入参/出参均为 `yyyy-MM-dd HH:mm:ss`，无需额外注解。如需时间戳格式，使用 Jackson 注解：

```java
@JsonFormat(shape = Shape.NUMBER)
private LocalDateTime shipTime;  // JSON 中为毫秒时间戳
```

## 健康探针

模块已内置 `spring-boot-starter-actuator`，开启配置即可使用：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,beans,httpexchanges
```

需额外注册 `HttpExchangeRepository`：

```java
@Bean
public HttpExchangeRepository httpExchangeRepository() {
    return new InMemoryHttpExchangeRepository();
}
```

```http
GET http://localhost:8088/actuator/health
```