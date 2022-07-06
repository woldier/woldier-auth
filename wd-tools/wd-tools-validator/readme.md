# validation 使用 指南
## 1.hibernate-validator常用注解

hibernate-validator提供的校验方式为在类的属性上加入相应的注解来达到校验的目的。hibernate-validator提供的用于校验的注解如下：

| 注解  | 说明  |
| --- | --- |
| @AssertTrue | 用于boolean字段，该字段只能为true |
| @AssertFalse | 用于boolean字段，该字段只能为false |
| @CreditCardNumber | 对信用卡号进行一个大致的验证 |
| @DecimalMax | 只能小于或等于该值 |
| @DecimalMin | 只能大于或等于该值 |
| @Email | 检查是否是一个有效的email地址 |
| @Future | 检查该字段的日期是否是属于将来的日期 |
| @Length(min=,max=) | 检查所属的字段的长度是否在min和max之间,只能用于字符串 |
| @Max | 该字段的值只能小于或等于该值 |
| @Min | 该字段的值只能大于或等于该值 |
| @NotNull | 不能为null |
| @NotBlank | 不能为空，检查时会将空格忽略 |
| @NotEmpty | 不能为空，这里的空是指空字符串 |
| @Pattern(regex=) | 被注释的元素必须符合指定的正则表达式 |
| @URL(protocol=,host,port) | 检查是否是一个有效的URL，如果提供了protocol，host等，则该URL还需满足提供的条件 |

## 2.使用步骤
wd-tools-validator没有提供全局异常处理，这是因为不同的系统对于校验结果的处理方式可能不一样，所以需要各个系统自己进行个性化的处理，而pd-tools-validator只是提供数据校验功能。

**具体使用过程：**

第一步：创建maven工程并配置pom.xml文件

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
    
<!-- 项目名自取   -->
    <groupId>com.woldier</groupId>
    <artifactId>myHibernateValidatorApp</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <dependencies>
        <dependency>
            <groupId>com.woldier</groupId>
            <artifactId>wd-tools-validator</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
```

第二步：创建User类

```java
package com.woldier.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.*;

@Data
public class User {
    @NotNull(message = "用户id不能为空")
    private Integer id;

    @NotEmpty(message = "用户名不能为空")
    @Length(max = 50, message = "用户名长度不能超过50")
    private String username;

    @Max(value = 80,message = "年龄最大为80")
    @Min(value = 18,message = "年龄最小为18")
    private int age;

    @Pattern(regexp = "[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$",
             message = "邮箱格式不正确")
    private String email;
}
```

第三步：创建UserController

```java
package com.woldier.entity;

import com.woldier.entity.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/user")
@Validated //开启校验功能
public class UserController {
    //简单数据类型校验
    @RequestMapping("/delete")
    public String delete(@NotBlank(message = "id不能为空") String id){
        System.out.println("delete..." + id);
        return "OK";
    }

    //对象属性校验
    @RequestMapping("/save")
    public String save(@Validated User user){
        System.out.println("save..." + user);
        return "OK";
    }
}
```

第四步：创建application.yml

```yaml
server:
  port: 9000
```

第五步：创建启动类

```java
package cn.itcast;

import com.woldier.auth.validator.config.EnableFormValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFormValidator //注解开启快速返回
public class MyHibernateValidatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyHibernateValidatorApplication.class,args);
    }
}
```

启动项目，访问地址：http://localhost:9000/user/save，可以看到控制台输出：

```makefile
2020-03-11 16:40:30.288  WARN 17428 --- [nio-9000-exec-4] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.validation.BindException: org.springframework.validation.BeanPropertyBindingResult: 1 errors
Field error in object 'user' on field 'age': rejected value [0]; codes [Min.user.age,Min.age,Min.int,Min]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [user.age,age]; arguments []; default message [age],18]; default message [年龄最小为18]]
```

页面效果：

这说明已经开始进行输入校验了，而且根据控制台输出可以看出已经开启快速失败返回模式。

为了能够给页面一个友好的提示，也可以加入全局异常处理。
 
## 3.异常处理示例
```java
package com.woldier.validator.config;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

@ControllerAdvice(annotations = {RestController.class, Controller.class}) //切片  在加了RestController或者Controller注解的controller层组件上加入前置
@ResponseBody //表明返回json数据
public class ValidatorGlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public String constraintViolationExceptionHandler(ConstraintViolationException e ){ //这里可在参数处获得注入的所有资源
        String msg = "";
        Set<ConstraintViolation<?>> violations =
                e.getConstraintViolations();
        ConstraintViolation<?> next = violations.iterator().next();
        msg = next.getMessage();
        return msg;
    }

    @ExceptionHandler(BindException.class)
    public String BindExceptionHandler(BindException e ){
        return e.getBindingResult().getFieldError().getDefaultMessage();
    }



}

```