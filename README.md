# spring-boot-nebula

##  解决问题
1. 统一公司所有`spring boot`项目的依赖管理 
 
不使用`common`可能存在的问题: 
- a项目使用了 redission 3.14 b项目 使用3.61,然后导致相同代码可能运行结果不一致
- 统一使用`web-spring-boot-start`模块或者`boot-common-parent`可解决不同项目依赖版本不一致问题。
在`boot-common-parent`管理公司的所有依赖，以后应用项目无需手动指定各种依赖版本只需引用依赖即可，统一在`boot-common-parent`管理即可
4. 提供开箱即用的`web-spring-boot-start`模块