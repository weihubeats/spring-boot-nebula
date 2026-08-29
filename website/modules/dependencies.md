# BOM 依赖 `spring-boot-nebula-dependencies`

统一管理所有 Spring Boot 项目的第三方依赖版本（Spring Boot、MyBatis-Plus、PageHelper、Redisson 等）。应用项目只需在 `dependencyManagement` 中 import BOM，引入依赖时**无需再手动指定版本**。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.weihubeats</groupId>
            <artifactId>spring-boot-nebula-dependencies</artifactId>
            <version>3.0.6</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

同时也将 Nebula 各模块版本统一管理，配合 CI-Friendly 的 `${revision}` 机制，父 POM 升版本即可全量对齐。