# Feign 自动解包 `spring-boot-nebula-feign`

OpenFeign 编解码扩展：自动将下游统一响应 `NebulaResponse<T>` 解包为业务对象 `T`。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-feign</artifactId>
</dependency>
```

## 使用方式

```java
@EnableFeignClients
@SpringBootApplication
public class Application { }

@FeignClient(name = "userClient", url = "${feign.client.user.url}")
public interface UserClient {

    // 直接返回业务对象，Decoder 自动解包 NebulaResponse
    @GetMapping("/provider/users/{id}")
    UserVO getUser(@PathVariable("id") Long id);

    // 显式声明 NebulaResponse 时不做解包
    @GetMapping("/provider/users/{id}")
    NebulaResponse<UserVO> getUserRaw(@PathVariable("id") Long id);
}
```

非成功码会走 `NebulaResponse#data()` 逻辑，抛出 `BizException` 或 `RpcException`。

HTTP 非 2xx 时由 `NebulaFeignErrorDecoder` 解析响应体中的 `NebulaResponse` 异常码，规则同上；无法解析则回退 Feign 默认行为。

## 慢调用告警

默认开启统一 Feign 日志过滤器（请求 URL/参数体、响应状态/体、耗时），并支持慢调用告警：

```yaml
nebula:
  feign:
    log:
      enabled: true
      level: INFO                 # 普通请求日志级别
      slow:
        enabled: true
        threshold-millis: 3000    # 超过该毫秒数打慢调用告警，默认 3000（3 秒）
        level: ERROR              # 慢调用告警日志级别
```

日志示例：
- 普通：`Feign GET http://host/api?id=1 cost=12ms requestBody= responseStatus=200 responseBody={...}`
- 慢调用：`Feign slow call alert POST http://host/api cost=3201ms threshold=3000ms requestBody={...}`

示例模块：`spring-boot-nebula-feign-sample`。