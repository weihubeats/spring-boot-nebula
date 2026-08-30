# Logging & Alerting `spring-boot-nebula-logback`

Complements the Web global exception monitor: `NebulaErrorMonitor` covers uncaught exceptions, while `spring-boot-nebula-logback` covers `log.error(...)` calls in business code.

## 1. Add the Dependency

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-logback</artifactId>
    <version>3.0.6</version>
</dependency>
```

## 2. Log Masking

Logback only registers the Converter; rules are configured in `application.yaml`:

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

Built-in rules: `mobile` / `idCard` / `bankCard` / `email` / `secretKey`.

## 3. Feishu Alerts for ERROR Logs

Configure in `application.yaml` (not in logback.xml):

```yaml
nebula:
  log:
    feishu:
      enabled: true
      webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      title: my-app
      max-per-minute: 10
```

When enabled, `NebulaLogAutoConfiguration` automatically attaches the `FeishuErrorAppender`. The webhook is independent of `nebula.web.monitor.url`; the same address may be used for both.

Runnable sample: `spring-boot-nebula-samples/spring-boot-nebula-logback-sample` (`GET /log/desensitize`, `GET /log/error`).
