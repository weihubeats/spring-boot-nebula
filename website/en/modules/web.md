# Web Wrapper `spring-boot-nebula-web`

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web</artifactId>
    <version>3.0.6</version>
</dependency>
```

## Unified Response `@NebulaResponseBody`

Controllers return business objects directly; the framework wraps them into a unified JSON automatically:

```java
@GetMapping("/test")
@NebulaResponseBody
public String test() {
    return "小奏";
}
```

**Class-level annotation is supported** — it applies to every method of the controller, no need to annotate each one:

```java
@NebulaResponseBody
@RestController
@RequestMapping("/users")
public class UserController { ... }
```

Rules:

| Scenario | Behavior |
|----------|----------|
| Annotated on both method and class | Method wins (e.g. the method specifies a different `objectMapper`) |
| Return value is already a `NebulaResponse` | Not wrapped again |
| Custom serialization | Point the `objectMapper()` attribute to an `ObjectMapper` subclass; configurable on class or method |

## Feign / RPC Calls

[Spring Boot Nebula Feign](/en/modules/feign) is recommended: declare the business return type directly on Feign methods, and the framework unwraps `NebulaResponse` automatically.

Without that module, you can also receive `NebulaResponse` manually and pull data via `data()` (which validates the status code).

## Custom Response Codes

Internal error codes remain `int`; this only affects the `code` field in the external JSON:

```yaml
# Success code as a string (for legacy compatibility)
nebula.web.response-code: Success

# Unified success/failure mapping (recommended)
nebula.web.code-mapping:
  200: Success
  400: Failure
  500: Error
```

Without configuration, numeric codes are returned, e.g. `"code": 200`.

## Pagination Objects

- Query parameters extend `NebulaPageQuery`
- Return results use `NebulaPageRes`

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

## Unified Exception Handling

See `NebulaRestExceptionHandler` — common exceptions are automatically wrapped into the unified response format.

Enable Feishu exception alerting:

```yaml
nebula:
  web:
    monitor:
      open: true
      type: feishu
      url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      limit:
        enabled: true        # Enable alert rate limiting
        window-seconds: 60   # Rate-limit window (seconds)
        max-count: 3         # Max alerts for the same key within the window
        storage: local       # local=per-instance in-memory limiting; redis=shared across instances (requires RedissonClient)
        key-prefix: nebula:alert:rate:   # Used when storage=redis
        expire-seconds: 120  # Used when storage=redis; must be ≥ window-seconds
```

To customize alerting, implement the `NebulaErrorMonitor` interface to replace the default behavior; to add a channel, implement `NebulaAlertChannel` and wire it under `monitor.type`.

## Timestamp Parameter `@GetTimestamp`

`@GetTimestamp` converts timestamp parameters into `LocalDateTime` automatically.

## LocalDateTime Handling

The module formats `LocalDateTime` globally via `JacksonTimeModule`: JSON body fields are serialized/deserialized as `yyyy-MM-dd HH:mm:ss` with no extra annotations. For timestamp formats, use Jackson annotations:

```java
@JsonFormat(shape = Shape.NUMBER)
private LocalDateTime shipTime;  // Milliseconds timestamp in JSON
```

## Health Probes

`spring-boot-starter-actuator` is already built in — just enable the configuration:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,beans,httpexchanges
```

An `HttpExchangeRepository` must also be registered:

```java
@Bean
public HttpExchangeRepository httpExchangeRepository() {
    return new InMemoryHttpExchangeRepository();
}
```

```http
GET http://localhost:8088/actuator/health
```
