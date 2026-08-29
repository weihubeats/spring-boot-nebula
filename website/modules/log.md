# 日志与告警 `spring-boot-nebula-logback`

与 Web 全局异常监控互补：`NebulaErrorMonitor` 覆盖未捕获异常；`spring-boot-nebula-logback` 覆盖业务里的 `log.error(...)`。

## 1. 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-logback</artifactId>
    <version>3.0.6</version>
</dependency>
```

## 2. 日志脱敏

logback 仅注册 Converter，规则在 `application.yaml` 配置：

```xml
<conversionRule conversionWord="msg"
    converterClass="com.nebula.log.logback.desensitize.DesensitizeMessageConverter"/>
<pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger - %msg%n</pattern>
```

```yaml
nebula:
  log:
    desensitize:
      enabled: true
      disable-rules:
        - bankCard
        - email
```

内置规则：`mobile` / `idCard` / `bankCard` / `email` / `secretKey`。

## 3. ERROR 日志飞书报警

在 `application.yaml` 配置（不在 logback.xml）：

```yaml
nebula:
  log:
    feishu:
      enabled: true
      webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      title: my-app
      max-per-minute: 10
```

启用后由 `NebulaLogAutoConfiguration` 自动挂载 `FeishuErrorAppender`。webhook 与 `nebula.web.monitor.url` 相互独立，可填同一地址。

可运行示例：`spring-boot-nebula-samples/spring-boot-nebula-logback-sample`（`GET /log/desensitize`、`GET /log/error`）。