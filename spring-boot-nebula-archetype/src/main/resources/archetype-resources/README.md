# ${rootArtifactId}

由 [spring-boot-nebula-archetype](https://github.com/weihubeats/spring-boot-nebula) 生成的轻量 DDD 四层服务。

## 生成方式

```bash
mvn archetype:generate \
    -DarchetypeGroupId=io.github.weihubeats \
    -DarchetypeArtifactId=spring-boot-nebula-archetype \
    -DarchetypeVersion=<nebula 版本> \
    -DgroupId=com.example \
    -DartifactId=demo \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackage=com.example.demo \
    -DinteractiveMode=false
```

## 分层架构（多模块）

```
${rootArtifactId}
├── ${rootArtifactId}-start            # 启动模块：Application、controller/vo、RPC provider、配置，唯一可执行 jar
├── ${rootArtifactId}-application      # 应用层：用例编排（AppService）、dto
├── ${rootArtifactId}-domain           # 领域层：model（业务规则内聚）、gateway 接口
├── ${rootArtifactId}-infrastructure   # 基础设施层：gateway 实现、mapper/dataobject
├── ${rootArtifactId}-api              # 对外契约：Dubbo/Feign 接口定义 + 出入参 DTO，供其他系统引用
└── ${rootArtifactId}-common           # 项目内通用：工具类、常量，不承载业务语义
```

依赖方向：`start → application → domain ← infrastructure`，模块边界由编译器强制。

- 业务规则写在 `domain/model`（如 `SysUser.create()` 的校验）
- `domain/gateway` 定义持久化契约，`infrastructure` 实现 —— 领域层零框架依赖
- VO 转换在 start 模块接口层完成，application 返回领域对象
- 新增用例时按层落位，禁止 start 直连 infrastructure

### 对外 RPC（api 模块）

- 接口与出入参 DTO 定义在 `${rootArtifactId}-api`，**刻意保持零框架依赖**——外部系统引用时不被拖入任何框架
- 实现放 start 的 `provider` 包（参考 `SysUserApiImpl`）：接入 Dubbo 加 `@DubboService`，接入 Feign 用 `@FeignClient`
- 对外 DTO 字段变更视为契约变更；需要对外暴露的错误码也放 api 模块

## 内置能力

| 能力 | 来源 | 说明 |
|---|---|---|
| 统一响应包装 | spring-boot-nebula-web | ResponseBodyAdvice 自动包 `NebulaResponse` |
| 全局异常处理 | spring-boot-nebula-web | RestExceptionHandler 自动装配 |
| 分页 | spring-boot-nebula-mybatis | 组合式 `NebulaPageQuery` + `PageHelperUtils`，支持多列排序 |
| 日志与链路追踪 | micrometer-tracing-bridge-otel | logback 模板每行输出 `traceId-spanId`；OTLP 上报见下 |
| 参数校验 | jakarta.validation | controller 上加 `@Valid` |

日志默认通过 Boot 官方 `%correlationId` 转换符输出 `traceId-spanId`（每次 HTTP 请求自动生成，跨线程传递请用 observation API）。接入 OpenTelemetry Collector：打开 `application.yaml` 中 `management.otlp.tracing.endpoint` 注释即可上报 OTLP。

## 运行

```bash
mvn spring-boot:run
# 默认 H2 内存库，schema.sql 自动初始化；http://localhost:8080/sys-users/page
```

切换 MySQL：替换 datasource 配置并删除 h2 依赖即可。

## 可选扩展模块

按需在 pom 中追加（版本已由 BOM 管理）：

- `spring-boot-nebula-feign`：远程调用
- `spring-boot-nebula-excel`：导入导出
- `spring-boot-nebula-distribute-lock`：分布式锁（需 Redis）
- `spring-boot-nebula-dynamic-datasource`：多数据源
- `spring-boot-nebula-log`：日志

## 构建

```bash
mvn verify            # 含 contextLoads 测试
docker build -t ${rootArtifactId} .
```

## K8s 部署

清单位于 `deploy/k8s/`（deployment.yaml + service.yaml）：

- **Java Agent 挂载**：init-container 启动时下载 OpenTelemetry Java Agent（自动埋点上报 OTLP）与 Alibaba TTL Agent（`transmittable-thread-local`，线程池场景传递 ThreadLocal/链路上下文）到共享 `emptyDir`，主容器通过 `JAVA_TOOL_OPTIONS` 以 `-javaagent` 只读挂载加载——镜像保持纯净，agent 版本改 env 即可升级
- **探针**：readiness/liveness 使用 actuator health group
- **上报地址**：默认指向集群内 `otel-collector.observability.svc.cluster.local:4317`，按实际环境修改
- 应用内 SDK 级导出默认关闭（见 application.yaml），与 agent 不双报；不用 agent 改走应用内导出时打开该注释即可
- 无线程池传递诉求时可从 `JAVA_TOOL_OPTIONS` 移除 ttl-agent
