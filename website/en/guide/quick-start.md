# Quick Start

## Option 1: Generate with the Archetype (Recommended)

Best for new services. **Run the command in the parent directory where you want the project placed** (e.g. `~/projects`); the generator creates a new folder named after the `artifactId` in the current directory:

```bash
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

Once generated, the project runs out of the box:

```bash
cd demo && mvn verify && mvn spring-boot:run -pl demo-start
```

See [Project Archetype](/en/modules/archetype) for details.

## Option 2: Manual Integration

### 1. Import the BOM (Recommended)

Manage versions centrally in the parent POM to avoid inconsistent dependency versions across projects (e.g. behavioral differences between Redisson 3.14 and 3.61):

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

### 2. Add the Web Module

```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web</artifactId>
</dependency>
```

### 3. Write the Application Class

```java
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(WebApplication.class, args);
    }
}
```

### 4. Write an Endpoint

No manual `Response` wrapping needed — just annotate the controller method with `@NebulaResponseBody` (it can also be placed on the class to apply to all methods):

```java
@GetMapping("/test")
@NebulaResponseBody
public String test() {
    return "小奏";
}
```

Response format:

```json
{
  "code": 200,
  "data": "小奏",
  "msg": "success"
}
```

### 5. Add More Modules as Needed

| Capability | ArtifactId | Docs |
|------------|------------|------|
| MyBatis-Plus wrapper | `spring-boot-nebula-mybatis` | [View](/en/modules/mybatis) |
| Read/write splitting | `spring-boot-nebula-dynamic-datasource` | [View](/en/modules/dynamic-datasource) |
| Distributed lock | `spring-boot-nebula-distribute-lock` | [View](/en/modules/distribute-lock) |
| Excel import/export | `spring-boot-nebula-excel` | [View](/en/modules/excel) |
| Region-routing JOIN | `spring-boot-nebula-join` | [View](/en/modules/join) |
| Feign unwrapping | `spring-boot-nebula-feign` | [View](/en/modules/feign) |
| Log masking & alerting | `spring-boot-nebula-logback` | [View](/en/modules/log) |
| DDD aggregate root | `spring-boot-nebula-aggregate` | [View](/en/modules/aggregate) |

## Run Samples Locally

Every capability ships with a runnable minimal sample under `spring-boot-nebula-samples`:

```bash
cd spring-boot-nebula-samples/spring-boot-nebula-web-sample
mvn spring-boot:run
```
