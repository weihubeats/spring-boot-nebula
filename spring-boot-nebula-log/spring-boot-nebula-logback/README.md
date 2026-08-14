# spring-boot-nebula-logback

Logback 扩展：日志脱敏 + ERROR 级飞书富文本告警。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-logback</artifactId>
    <version>${nebula.version}</version>
</dependency>
```

## 1. 日志脱敏

在 `logback.xml`（或 `logback-spring.xml`）注册 Converter，让 `%msg` 走脱敏：

```xml
<conversionRule conversionWord="msg"
    converterClass="com.nebula.log.logback.desensitize.DesensitizeMessageConverter"/>

<property name="LOG_PATTERN"
    value="%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger - %msg%n"/>
```

业务代码无需改动，照常 `log.info(...)` / `log.error(...)`。

规则与开关在 `application.yaml` 配置：

```yaml
nebula:
  log:
    desensitize:
      enabled: true
      # 不脱敏的 Spring profile，默认 dev、test；配空列表 [] 则所有环境都脱敏
      disabled-environments:
        - dev
        - test
      disable-rules:
        - bankCard
        - email
```

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `nebula.log.desensitize.enabled` | `true` | 是否启用脱敏 |
| `nebula.log.desensitize.disabled-environments` | `dev, test` | 命中当前激活的 Spring profile 时跳过脱敏（便于本地调试），配 `[]` 则不跳过 |
| `nebula.log.desensitize.disable-rules` | 空 | 要关闭的内置规则名 |

内置规则：`mobile`、`idCard`、`bankCard`、`email`、`secretKey`（匹配 `password` / `pwd` / `token` / `secret` / `accessKey`）。

### 自定义扩展规则

实现 `DesensitizeRule`（可继承 `RegexDesensitizeRule`），注册为 Spring Bean 即可追加到内置规则之后：

```java
@Bean
public DesensitizeRule orderIdDesensitizeRule() {
    return new RegexDesensitizeRule(
            "orderId",
            Pattern.compile("ORD-\\d{4}"),
            m -> "ORD-****");
}
```

也可通过 Java SPI：在 `META-INF/services/com.nebula.log.logback.desensitize.DesensitizeRule` 写入实现类全名。

自定义规则同样可用 `nebula.log.desensitize.disable-rules` 按 `name()` 关闭。

## 2. ERROR 飞书告警

无需在 `logback.xml` 中声明 Appender。开启后由自动配置挂到 root logger，对 `log.error(...)` 发送飞书 interactive 卡片。

```yaml
nebula:
  log:
    feishu:
      enabled: true
      webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      title: my-app
      max-per-minute: 10
```

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `nebula.log.feishu.enabled` | `false` | 是否开启 ERROR 飞书告警 |
| `nebula.log.feishu.webhook-url` | — | 飞书机器人 webhook（必填） |
| `nebula.log.feishu.title` | `nebula` | 卡片标题前缀 |
| `nebula.log.feishu.max-per-minute` | `10` | 每分钟最多发送条数（防刷） |
| `nebula.log.feishu.queue-size` | `256` | 异步发送队列容量 |

告警为异步发送；队列满或超限流时丢弃，失败只写 Logback Status，避免递归打 error。

与 `spring-boot-nebula-web` 的全局异常飞书监控（`nebula.web.monitor.*`）相互独立，可共用同一 webhook。

## 完整配置示例

```yaml
nebula:
  log:
    desensitize:
      enabled: true
    feishu:
      enabled: true
      webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
      title: my-app
      max-per-minute: 10
```

## 可运行示例

仓库内：`spring-boot-nebula-samples/spring-boot-nebula-logback-sample`

- `GET /log/desensitize`：查看控制台脱敏效果
- `GET /log/error`：触发 ERROR 飞书告警（需配置真实 webhook）
