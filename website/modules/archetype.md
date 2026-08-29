# 脚手架 `spring-boot-nebula-archetype`

Maven Archetype 脚手架，一条命令生成基于 Nebula 的**多模块轻量 DDD** 项目骨架，开箱即含统一响应、全局异常、参数校验与分页。

## 一键生成

**在你想放置项目的父目录下执行**（任意空目录即可，如 `~/projects`），生成器会在当前目录创建以 `-DartifactId` 命名的新文件夹：

```bash
cd ~/projects                 # 项目将生成在 ~/projects/demo
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

生成完成后项目位于 `./demo/`，即 `demo/pom.xml`、`demo/demo-start/...`。注意：目标目录必须不存在，否则报 `already exists` 拒绝覆盖。

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `groupId` | 项目 groupId | `com.example` |
| `artifactId` | 项目名，同时作为各子模块前缀 | 必填 |
| `version` | 项目版本 | `1.0.0-SNAPSHOT` |
| `package` | 基础包名 | `com.example` |

## 生成的项目结构

以 `demo` 为例：

```
demo/
├── pom.xml                    # 父 pom：统一依赖管理（BOM import）
├── demo-start                 # 启动模块：Application、controller/vo、RPC provider、配置文件
├── demo-application           # 应用层：用例编排（AppService）、DTO
├── demo-domain                # 领域层：领域模型（业务规则）、gateway 接口
├── demo-infrastructure        # 基础设施层：gateway 实现、mapper/dataobject
├── demo-api                   # 对外契约：Dubbo/Feign 接口定义 + 出入参 DTO，供其他系统引用
├── demo-common                # 项目内通用：工具类、常量，不承载业务语义
├── Dockerfile                 # 多阶段构建，产出可执行镜像
├── deploy/k8s/                # K8s 部署清单（deployment + service）
└── .github/workflows/ci.yaml  # GitHub Actions：mvn verify
```

依赖方向由编译器强制：

```text
start ──▶ application ──▶ domain ◀── infrastructure
```

- `start` 是唯一可执行 jar（spring-boot-maven-plugin repackage）
- `domain` 只依赖 `nebula-common`（分页模型），零框架侵入
- `infrastructure` 实现 `domain/gateway` 接口 —— 依赖倒置
- VO 转换收敛在 start 的接口层，application 返回领域对象

## 分层职责约定

| 层 | 放什么 | 不放什么 |
|----|--------|----------|
| interfaces（start） | controller、VO、协议转换、全局配置 | 业务逻辑 |
| application | 用例编排、事务边界、DTO | 业务规则 |
| domain | 实体工厂/行为、业务校验、gateway 接口 | 框架依赖、SQL |
| infrastructure | gateway 实现、MyBatis-Plus mapper、DO | 业务规则 |

新增一个用例的标准动线：`controller 方法 → AppService 方法 → gateway 接口 → gatewayImpl`。

### 对外 RPC（api 模块）

- 接口与出入参 DTO 定义在 `demo-api`，**刻意保持零框架依赖**——外部系统只引用这一个 jar，不被拖入任何框架
- 实现放 start 的 `provider` 包（参考 `SysUserApiImpl`）：接入 Dubbo 加 `@DubboService`，接入 Feign 用 `@FeignClient`
- 对外 DTO 字段变更视为契约变更；需要对外暴露的错误码也放 api 模块
- 通用工具类、常量放 `demo-common`（仅项目内部使用，不对外发布）

## 内置能力

| 能力 | 来源 | 说明 |
|------|------|------|
| 统一响应包装 | nebula-web | controller 返回裸类型，自动包成 `NebulaResponse` |
| 全局异常处理 | nebula-web | RestExceptionHandler 自动装配 |
| 参数校验 | starter-validation + jakarta.validation | controller 加 `@Valid` 即生效 |
| 分页 | nebula-mybatis | 组合式 `NebulaPageQuery` + `PageHelperUtils`，支持多列排序 |
| 日志与链路追踪 | micrometer-tracing-bridge-otel | logback 模板每行输出 `traceId-spanId`，OTLP 上报按需开启 |
| 示例链路 | SysUser CRUD | 含分页查询的完整四层代码示范 |

### 日志与 OpenTelemetry

内置 `logback-spring.xml`，通过 Boot 官方 `%correlationId` 转换符，每条日志自动携带链路标识：

```text
2026-08-26 10:00:00.123 INFO  [http-nio-8080-exec-1] [4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7] c.e.d.controller.SysUserController - ...
```

- traceId/spanId 由 micrometer-tracing（OpenTelemetry API 桥接）在每次 HTTP 请求自动生成并贯穿调用链，采样率 `management.tracing.sampling.probability`
- 默认**只打 tid 不上报**（零噪音）；接入 Collector 时打开 `application.yaml` 中 `management.otlp.tracing.endpoint` 即可导出 OTLP
- 生产 profile 追加异步滚动文件日志（`./logs/{app}.log`），默认仅控制台

## 运行与测试

默认 H2 内存库，`schema.sql` 自动初始化两条示例数据：

```bash
mvn verify                      # 构建 + contextLoads 测试
mvn spring-boot:run -pl demo-start

curl "http://localhost:8080/sys-users/page?page.pageSize=1&page.pageIndex=1"
curl -X POST http://localhost:8080/sys-users -H 'Content-Type: application/json' -d '{"name":"carol","age":25}'
```

分页参数为嵌套结构：`page.pageSize` / `page.pageIndex`；排序传 `page.sorts[0].column` 与 `page.sorts[0].direction`（列名白名单校验，防 SQL 注入）。

## 切换 MySQL

替换 `demo-start/src/main/resources/application.yaml` 的 datasource，删除 h2 依赖即可：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo
    username: root
    password: xxx
```

## 扩展更多能力

版本已由父 pom 的 BOM 统一管理，按需在对应模块追加依赖：

| 能力 | ArtifactId | 建议放置模块 |
|------|------------|--------------|
| Feign 自动解包 | `spring-boot-nebula-feign` | start |
| Excel 导入导出 | `spring-boot-nebula-excel` | start |
| 分布式锁（需 Redis） | `spring-boot-nebula-distribute-lock` | start |
| 动态数据源/读写分离 | `spring-boot-nebula-dynamic-datasource` | infrastructure |

## 升级 Nebula 版本

生成的父 pom 中有独立属性，改一处即全项目生效：

```xml
<properties>
    <nebula.version>3.0.6</nebula.version>
</properties>
```

## Docker 部署

```bash
docker build -t demo .
docker run -p 8080:8080 demo
```

## K8s 部署

清单位于 `deploy/k8s/`（deployment.yaml + service.yaml），核心设计：

- **Java Agent 挂载**：init-container 启动时下载两个 agent 到共享 `emptyDir`，主容器经 `JAVA_TOOL_OPTIONS` 只读挂载加载，镜像保持纯净，升级 agent 只需改 env 中的版本号：
  - OpenTelemetry Java Agent —— 自动埋点，OTLP 上报（默认地址 `otel-collector.observability.svc.cluster.local:4317`）
  - Alibaba TTL Agent（transmittable-thread-local）—— 线程池场景传递 ThreadLocal 与链路上下文；无此诉求可从 `JAVA_TOOL_OPTIONS` 移除
- **探针**：readiness/liveness 走 actuator health group
- **不双报**：应用内 SDK 导出默认关闭（application.yaml 中注释态），agent 模式与应用内导出二选一
