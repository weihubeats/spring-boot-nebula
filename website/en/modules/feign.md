# Feign Unwrapping `spring-boot-nebula-feign`

An OpenFeign codec extension: automatically unwraps the downstream unified response `NebulaResponse<T>` into the business object `T`.

## Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-feign</artifactId>
</dependency>
```

## Usage

```java
@EnableFeignClients
@SpringBootApplication
public class Application { }

@FeignClient(name = "userClient", url = "${feign.client.user.url}")
public interface UserClient {

    // Returns the business object directly; the Decoder unwraps NebulaResponse automatically
    @GetMapping("/provider/users/{id}")
    UserVO getUser(@PathVariable("id") Long id);

    // No unwrapping when NebulaResponse is declared explicitly
    @GetMapping("/provider/users/{id}")
    NebulaResponse<UserVO> getUserRaw(@PathVariable("id") Long id);
}
```

Non-success codes go through the `NebulaResponse#data()` logic, throwing `BizException` or `RpcException`.

For HTTP non-2xx responses, `NebulaFeignErrorDecoder` parses the `NebulaResponse` error code from the response body with the same rules; if parsing fails, it falls back to Feign's default behavior.

## Slow-Call Alerting

A unified Feign logging filter is enabled by default (request URL/params/body, response status/body, elapsed time), with slow-call alerting support:

```yaml
nebula:
  feign:
    log:
      enabled: true
      level: INFO                 # Log level for normal requests
      slow:
        enabled: true
        threshold-millis: 3000    # Slow-call alert threshold in millis; default 3000 (3 seconds)
        level: ERROR              # Log level for slow-call alerts
```

Log examples:
- Normal: `Feign GET http://host/api?id=1 cost=12ms requestBody= responseStatus=200 responseBody={...}`
- Slow call: `Feign slow call alert POST http://host/api cost=3201ms threshold=3000ms requestBody={...}`

Sample module: `spring-boot-nebula-feign-sample`.
