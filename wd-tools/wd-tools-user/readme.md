# wd-tools-user使用

wd-tools-user的实现和我们上面的入门案例是一致的，都是通过自定义参数解析器来为Controller的方法注入当前登录用户对象。

实现思路：

1、定义LoginUser注解，用于标注在Controller的方法参数上

2、自定义拦截器，从请求头中获取用户信息并设置到上下文（通过ThreadLocal实现）中

3、自定义参数解析器，从上下文中获取用户信息并封装为SysUser对象给Controller的方法参数

4、定义配置类，用于注册自定义拦截器和参数解析器

注意：wd-tools-user模块并不是starter，所以如果要使用其提供的功能，需要在应用的启动类上加入@EnableLoginArgResolver注解。

**具体使用过程：**

第一步：创建maven工程myCurrentUserApp并配置pom.xml文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/>
    </parent>
    <groupId>com.woldier</groupId>
    <artifactId>myCurrentUserApp</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>com.woldier</groupId>
            <artifactId>wd-tools-user</artifactId>
            <version>1.0-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>com.alibaba.cloud</groupId>
                    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

第二步：编写启动类

```java
package com.woldier;

import com.woldier.auth.user.annotation.EnableLoginArgResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableLoginArgResolver //开启自动登录用户对象注入
public class MyCurrentUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyCurrentUserApplication.class,args);
    }
}
```

第三步：创建UserController

```java
package com.woldier.controller;

import com.woldier.auth.user.annotation.LoginUser;
import com.woldier.auth.user.model.SysUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/getCurrentUser")
    public SysUser getCurrentUser(@LoginUser SysUser user){//注入当前登录用户
        System.out.println(user);
        return user;
    }
}
```

启动项目，因为wd-tools-user模块需要从请求头中获取用户信息，所以需要使用postman进行测试：
url为：localhost:8080/user/getCurrentUser 
请求头 userid=111,account=admin,name=xiaoming (此处的请求头为gateway处理过后的请求头)