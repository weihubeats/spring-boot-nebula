# spring-boot-nebula-archetype

Nebula 脚手架模块：基于 Maven Archetype，一条命令生成基于 Nebula 组件库的**多模块轻量 DDD** 项目骨架，内置统一响应、全局异常、参数校验、分页与完整示例链路。

> 在线文档：https://weihubeats.github.io/spring-boot-nebula/modules/archetype

## 使用

在想放置项目的父目录执行（任意目录均可），生成器会在当前目录创建 `-DartifactId` 同名文件夹；目标目录已存在会拒绝覆盖：

```bash
cd ~/projects                 # 生成位置：~/projects/demo
mvn archetype:generate \
    -DarchetypeGroupId=io.github.weihubeats \
    -DarchetypeArtifactId=spring-boot-nebula-archetype \
    -DarchetypeVersion=3.0.5 \
    -DgroupId=com.example \
    -DartifactId=demo \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackage=com.example.demo \
    -DinteractiveMode=false
```

> 首次使用若本地仓库没有该 archetype，请先从 Maven Central 拉取（generate 会自动下载）。

生成结构（`artifactId` 同时作为子模块前缀）：

```
demo/
├── pom.xml                    # 父 pom：BOM import，统一依赖管理
├── demo-start                 # 启动模块：Application、controller/vo、RPC provider、配置
├── demo-application           # 应用层：用例编排、DTO
├── demo-domain                # 领域层：领域模型、gateway 接口（仅依赖 nebula-common）
├── demo-infrastructure        # 基础设施层：gateway 实现、mapper/DO
├── demo-api                   # 对外契约：Dubbo/Feign 接口定义 + DTO（零框架依赖）
├── demo-common                # 项目内通用：工具类、常量
├── deploy/k8s/                # K8s 清单：init-container 挂载 OTel + TTL agent、探针、service
├── Dockerfile                 # 多阶段构建
└── .github/workflows/ci.yaml  # CI：mvn verify
```

依赖方向由编译器强制：`start → application → domain ← infrastructure`，`start` 为唯一可执行 jar。

生成后即可验证与运行：

```bash
cd demo && mvn verify && mvn spring-boot:run -pl demo-start
```

## 模块内部结构

```text
spring-boot-nebula-archetype/
├── pom.xml                                            # packaging=maven-archetype
└── src/main/resources/
    ├── META-INF/maven/archetype-metadata.xml          # 生成器描述符（参数、modules、fileSets）
    └── archetype-resources/                           # 项目模板本体
        ├── pom.xml                                    # 生成的父 pom（含 <nebula.version> 属性）
        ├── __rootArtifactId__-*/                      # 六个子模块模板（目录名占位符）
        ├── Dockerfile / README.md / .gitignore / .github/
        └── ...
```

## 维护指南

### 验证改动

改模板后必须 `clean` 再安装（否则 `target/classes` 残留旧资源进入 jar），然后端到端验证：

```bash
mvn clean install -Dgpg.skip=true -DskipTests

# 本地 catalog 生成并构建
mvn archetype:generate -DarchetypeCatalog=local \
    -DarchetypeGroupId=io.github.weihubeats \
    -DarchetypeArtifactId=spring-boot-nebula-archetype \
    -DarchetypeVersion=<当前版本> \
    -DgroupId=com.example -DartifactId=e2e-check \
    -Dpackage=com.example.e2e -DinteractiveMode=false
cd e2e-check && mvn verify
```

### 发版同步

模板父 pom 中 `<nebula.version>` 为硬编码字面量（Archetype 机制限制，无法引用构建属性），发新版本时需手动同步为最新 nebula 版本。

### 已知机制约束

| 约束 | 说明 |
|------|------|
| 目录占位符 | 子模块目录名必须用双下划线形式 `__rootArtifactId__`，并在 descriptor 的 `<modules>` 中登记 |
| 文件名 token | 文件名中的 `${...}` **不会**被替换，主类等使用固定名（如 `Application.java`） |
| module 内 artifactId 重绑 | 模块文件内 `${artifactId}` 解析为该模块自身 id，子 pom 的 `<parent>` 必须写 `${rootArtifactId}` |
| Velocity 冲突 | 模板内容中的 Spring 占位符（如 yaml 的 `${...}`）恰好是未定义 Velocity 引用时原样保留；GitHub Actions 的 `${{ }}` 需避免或转义 |

## License

Apache License 2.0
