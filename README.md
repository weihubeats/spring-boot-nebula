# spring-boot-nebula

开箱即用的web模块

不使用`spring-boot-nebula-web`搭建项目

需要引入spring boot 各种依赖，指定各种版本，然后编写各种分页基础对象，`Result`返回对象
比如需要 原先web项目需要使用的返回值比如`Response<T> ss`;
```java
    @GetMapping("/test")
    public Response<String> test() {
        return Response.success("小奏");
    }
```
使用`spring-boot-nebula-web`搭建项目如何简单
1. 引入依赖
```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web</artifactId>
    <version>0.0.2</version>
</dependency>
```
2. 编写一个启动类
```java
@SpringBootApplication
    public class WebApplication {

    public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    SpringApplication.run(WebApplication.class, args);
    }
    }
```

然后就可以愉快的写接口了,返回值无需使用`Response`包装，仅仅需要添加`@NebulaResponseBody`注解
```java
    @GetMapping("/test")
    @NebulaResponseBody
    public String test() {
        return "小奏";
    }
```
返回结果
```json
{
  "code": 200,
  "data": "小奏",
  "msg": "success"
}
```

##  功能
1. 统一公司所有`spring boot`项目的依赖管理 
 
不使用`spring-boot-nebula-dependencies`可能存在的问题: 
- a项目使用了 redission 3.14 b项目 使用3.61,然后导致相同代码可能运行结果不一致
  - 统一使用`spring-boot-nebula-dependencies`作为p'a'r'a'm
  在`boot-common-parent`管理公司的所有依赖，以后应用项目无需手动指定各种依赖版本只需引用依赖即可，统一在`boot-common-parent`管理即可
2. 提供开箱即用的`web-spring-boot-start`模块，解决web开发需要手动封装工具类的痛点
3. 提供统一异常处理
4. 提供优雅的时间戳转`LocalDateTime`注解
5. 提供开箱即用的分页对象
6. 提供开箱即用的分布式锁
7. 提供开箱即用的`mybatis-plus`模块
8. 提供开箱即用的`ddd`聚合根模块


## demo
使用参考 [spring-boot-nebula-samples](spring-boot-nebula-samples)模块

### [spring-boot-nebula-web](spring-boot-nebula-web) 使用

1. 引入依赖
```xml
 <dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web</artifactId>
    <version>0.0.2</version>
</dependency>
```

1. 运行[Application.java](spring-boot-nebula-samples%2Fspring-boot-nebula-web-sample%2Fsrc%2Fmain%2Fjava%2Fcom%2Fnebula%2Fweb%2Fsample%2FApplication.java)
2. 运行 [http-test-controller.http](spring-boot-nebula-samples%2Fspring-boot-nebula-web-sample%2Fsrc%2Fmain%2Fhttp%2Fhttp-test-controller.http)中的`GET localhost:8088/test`



现在不需要将自己的返回对象包裹起来,只需要添加注解`@NebulaResponseBody`


#### 提供开箱即用的分页对象

- [NebulaPageRes.java](spring-boot-nebula-common%2Fsrc%2Fmain%2Fjava%2Fcom%2Fnebula%2Fbase%2Fmodel%2FNebulaPageRes.java)

```java
    @GetMapping("/list")
    public NebulaPageRes<StudentVO> list(StudentDTO studentDTO) {
        return studentService.list(studentDTO);
}
```

分页查询继承[NebulaPageQuery.java](spring-boot-nebula-common%2Fsrc%2Fmain%2Fjava%2Fcom%2Fnebula%2Fbase%2Fmodel%2FNebulaPageQuery.java)即可

```java

@GetMapping("/list")
public NebulaPageRes<StudentVO> list(StudentDTO studentDTO) {
  return studentService.list(studentDTO);
}

@Data
public class StudentDTO extends NebulaPageQuery {
    
    private Long id;
    
    private String name;
    
    private Integer age;
}

```


#### 时间戳自动转`LocalDateTime`注解
@GetTimestamp

```java
    @GetMapping("/test")
    @NebulaResponseBody
    public String test(@GetTimestamp LocalDateTime time) {
        return time.toString();
    }
```

# 依赖 

- web
```xml

<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-web</artifactId>
    <version>0.0.2</version>
</dependency>
```

- 分布式锁
```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-distribute-lock</artifactId>
    <version>0.0.2</version>
</dependency>
```

- ddd聚合根组件
```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-aggregate</artifactId>
    <version>0.0.2</version>
</dependency>
```

- mybatis-plus
```xml
<dependency>
    <groupId>io.github.weihubeats</groupId>
    <artifactId>spring-boot-nebula-mybatis-plus</artifactId>
    <version>0.0.2</version>
</dependency>
```

# 模块说明
- [spring-boot-nebula-common](spring-boot-nebula-common) 基础工具组件
- [spring-boot-nebula-aggregate](spring-boot-nebula-aggregate) ddd聚合根组件
- [spring-boot-nebula-dependencies](spring-boot-nebula-dependencies) 统一依赖
- [spring-boot-nebula-samples](spring-boot-nebula-samples) 使用示例
- [spring-boot-nebula-web](spring-boot-nebula-web) web封装组件(包括统一异常返回，简化返回，自定义异常报警)
- [spring-boot-nebula-web-common](spring-boot-nebula-web-common) web模块基础工具类
- [spring-boot-nebula-distribute-lock](spring-boot-nebula-distribute-lock) 分布式锁
- [spring-boot-nebula-mybatis](spring-boot-nebula-mybatis) mybatis的一些封装，比如提供基础的`BaseDO`，一些常用的类型处理器，比如数组

## [spring-boot-nebula-web-common](spring-boot-nebula-web-common)
- 提供[SpringBeanUtils.java](spring-boot-nebula-web-common%2Fsrc%2Fmain%2Fjava%2Fcom%2Fnebula%2Fweb%2Fcommon%2Futils%2FSpringBeanUtils.java)获取spring bean
- 提供[NebulaSysWebUtils.java](spring-boot-nebula-web-common%2Fsrc%2Fmain%2Fjava%2Fcom%2Fnebula%2Fweb%2Fcommon%2Futils%2FNebulaSysWebUtils.java) 获取spring 环境信息
- 提供[ExpressionUtil.java](spring-boot-nebula-web-common%2Fsrc%2Fmain%2Fjava%2Fcom%2Fnebula%2Fweb%2Fcommon%2Futils%2FExpressionUtil.java) 解析el表达式

