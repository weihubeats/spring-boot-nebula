# BOM Dependencies `spring-boot-nebula-dependencies`

Centrally manages third-party dependency versions for all Spring Boot projects (Spring Boot, MyBatis-Plus, PageHelper, Redisson, etc.). Applications only need to import the BOM in `dependencyManagement` — **no more hand-specifying versions** when adding dependencies.

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

The BOM also centrally manages the versions of all Nebula modules. Combined with the CI-Friendly `${revision}` mechanism, bumping the version in the parent POM aligns everything at once.
