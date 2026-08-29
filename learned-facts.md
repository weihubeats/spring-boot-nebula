# 动态学习事实

## 构建环境
- 本机默认 JDK 为 Homebrew OpenJDK 26，跑 Maven 时 Lombok 注解处理静默失效（`log` 符号找不到等假编译错误）。必须用 `export JAVA_HOME=$(/usr/libexec/java_home -v 17)` 再执行 mvn。
- 父 POM 的 gpg 插件在本地构建会因缺 gpg 环境失败，需加 `-Dgpg.skip=true`。

## 分页模型（2026-08 重构后）
- 分页抽象位于 `com.nebula.base.pagination`（旧 `com.nebula.base.model.NebulaPageQuery/NebulaPageRes` 已删除）。
- `NebulaPageQuery` 是 final 具体类，业务 DTO 用组合持有：`private NebulaPageQuery page = new NebulaPageQuery();`，DAO 层调 `PageHelperUtils.startPage(dto.getPage())`。
- `NebulaPageRes` 是 record（不可变），组件：list/totalCount/pageSize/pageIndex；排序用 `List<Sort>`（列名白名单 `^[a-zA-Z0-9_.]+$` 防 SQL 注入），PageHelperUtils.startPage 会把 sorts 拼到 PageHelper 的 orderBy。

## 脚手架（spring-boot-nebula-archetype）
- Maven Archetype 模块，`mvn archetype:generate -DarchetypeArtifactId=spring-boot-nebula-archetype` 生成**多模块**轻量 DDD 项目：`{rootArtifactId}-start/-application/-domain/-infrastructure/-api/-common`，依赖方向 start→application→domain←infrastructure，start 为唯一可执行 jar。
- `-api`：对外 RPC 契约（Dubbo/Feign 接口 + DTO），刻意零框架依赖（仅 jakarta.validation-api），外部系统只引此 jar；实现放 start 的 `provider` 包。
- `-common`：项目内通用工具/常量，无依赖；对外错误码放 api 不放 common。
- K8s 清单 `deploy/k8s/`：init-container 下载 OTel javaagent + Alibaba TTL agent（transmittable-thread-local 主 jar 即 agent）到共享 emptyDir，主容器 `JAVA_TOOL_OPTIONS` 只读挂载；应用内 SDK 导出默认关闭避免与 agent 双报；探针走 actuator health group。yaml 中 `${OTEL_AGENT_VERSION}` 等留给容器 shell 展开，Velocity 因引用名合法而原样保留。
- 日志/链路：start 模板带 logback-spring.xml，用 Boot 官方 `%correlationId` 转换符（需 conversionRule 指向 CorrelationIdConverter）输出 `traceId-spanId`；依赖 micrometer-tracing-bridge-otel + opentelemetry-exporter-otlp（Boot BOM 管版本）；`management.otlp.tracing.endpoint` 不配置则不上报、零噪音。**不要用 `%X{traceId}` MDC 方案**——Boot 3.4 actuator 不注册 TracingAwareLoggingObservationHandler，MDC 恒空。
- **Archetype + Velocity 陷阱**：模板资源里 `${VAR:-default}` 这类 logback 默认值语法会让 Velocity 直接解析报错（generate 失败），`:-` 序列必须避免；合法引用名的未定义变量（如 `${APP_NAME}`）才原样保留。
- 多模块 archetype 关键机制：descriptor 用 `<modules><module id="${rootArtifactId}-x" dir="__rootArtifactId__-x">`，目录名用双下划线占位才会被替换；module 上下文中 `artifactId` 被重绑为模块自身 id，子 pom 的 `<parent>` 必须引用 `${rootArtifactId}`。
- 文件名 token（`${rootArtifactId}Application.java`）不会被替换，主类用固定名 Application.java；改资源后必须 `clean install`，否则 target/classes 幽灵文件进 jar。
- 骨架依赖：nebula-web(start) + nebula-mybatis(infrastructure) + starter-validation(start) + jakarta.validation-api/spring-tx/spring-context(application) + nebula-common(domain) + h2(runtime)；nebula 版本硬编码在模板根 pom `<nebula.version>`，发版需手动同步。

## 重要存量 bug 记录
- **pagehelper-spring-boot-starter 1.4.2 在 Boot3 下分页静默失效**（autoconfigure 只有 spring.factories，无 AutoConfiguration.imports，拦截器不注册，total 恒为 0）。已升级至 1.4.7 / pagehelper 5.3.3 修复。排查这类问题的特征：Page 对象能拿到传入的 pageNum/pageSize，但 SQL 无 LIMIT、total=0。
- **Spring 6.1+ 必须 maven-compiler-plugin `<parameters>true</parameters>`**，否则 `@PathVariable/@RequestParam` 按参数名绑定直接 500（No parameter name available via reflection）。生成的项目模板父 pom 已配置；nebula 自身各模块 pom 也应确认。
- nebula-web 的 Redis(redisson) 是条件装配（`nebula.web.monitor.limit.storage=redis`），无 Redis 可正常启动；响应包装由 NebulaResponseBodyAdvice 自动完成，controller 直接返回裸类型。
