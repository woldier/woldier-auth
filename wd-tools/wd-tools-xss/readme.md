#  AntiSamy介绍

AntiSamy是OWASP的一个开源项目，通过对用户输入的 HTML / CSS / JavaScript 等内容进行检验和清理，确保输入符合应用规范。AntiSamy被广泛应用于Web服务对存储型和反射型XSS的防御中。

AntiSamy的maven坐标：

```xml
<dependency>
  <groupId>org.owasp.antisamy</groupId>
  <artifactId>antisamy</artifactId>
  <version>1.5.7</version>
</dependency>
```

### 1.AntiSamy入门案例

第一步：创建maven工程antiSamy_demo并配置pom.xml文件

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
    <artifactId>antiSamy_demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.owasp.antisamy</groupId>
            <artifactId>antisamy</artifactId>
            <version>1.5.7</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
```

第二步：创建application.yml

```yaml
server:
  port: 9000
```

第三步：创建策略文件/resources/antisamy-ebay.xml，文件内容可以从antisamy的jar包中获取

注：AntiSamy对“恶意代码”的过滤依赖于策略文件。策略文件规定了AntiSamy对各个标签、属性的处理方法，策略文件定义的严格与否，决定了AntiSamy对XSS漏洞的防御效果。在AntiSamy的jar包中，包含了几个常用的策略文件

我们从里面拷出antisamy-ebay.xml

---

第四步：创建User实体类

```java
package com.woldier.entity;

import lombok.Data;

@Data
public class User {
    private int id;
    private String name;
    private int age;
}
```

第五步：创建UserController

```java
package com.woldier.controller;

import cn.itcast.entity.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/save")
    public String save(User user){
        System.out.println("UserController save.... " + user);
        return user.getName();
    }
}
```

第六步：创建/resources/static/index.html页面

```html
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Title</title>
    </head>
    <body>
        <form method="post" action="/user/save">
            id:<input type="text" name="id"><br>
            name:<input type="text" name="name"><br>
            age:<input type="text" name="age"><br>
            <input type="submit" value="submit">
        </form>
    </body>
</html>
```

第七步：创建启动类

```java
package com.woldier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AntiSamyApp {
    public static void main(String[] args) {
        SpringApplication.run(AntiSamyApp.class,args);
    }
}
```

此时我们可以启动项目进行访问，但是还没有进行参数的过滤，所以如果我们输入任意参数都可以正常传递到Controller中，这在实际项目中是非常不安全的。为了对我们输入的数据进行过滤清理，需要通过过滤器来实现。

第八步：创建过滤器，用于过滤所有提交到服务器的请求参数

```java
package com.woldier.filter;

import cn.itcast.wrapper.XssRequestWrapper;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/*
*过滤所有提交到服务器的请求参数
*/
public class XssFilter implements Filter{
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        //传入重写后的Request
        filterChain.doFilter(new XssRequestWrapper(request),servletResponse);
    }
}
```

注意：通过上面的过滤器可以发现我们并没有在过滤器中直接进行请求参数的过滤清理，而是直接放行了，那么我们还怎么进行请求参数的过滤清理呢？其实过滤清理的工作是在另外一个类XssRequestWrapper中进行的，当上面的过滤器放行时需要调用filterChain.doFilter()方法，此方法需要传入请求Request对象，此时我们可以将当前的request对象进行包装，而XssRequestWrapper就是Request对象的包装类，在过滤器放行时会自动调用包装类的getParameterValues方法，我们可以在包装类的getParameterValues方法中进行统一的请求参数过滤清理。

第九步：创建XssRequestWrapper类

```java
package com.woldier.wrapper;

import org.owasp.validator.html.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class XssRequestWrapper extends HttpServletRequestWrapper {
    /*
     * 策略文件 需要将要使用的策略文件放到项目资源文件路径下
    */
    private static String antiSamyPath = XssRequestWrapper.class.getClassLoader()
            .getResource( "antisamy-ebay.xml").getFile();

    public static  Policy policy = null;
    static {
        // 指定策略文件
        try {
            policy = Policy.getInstance(antiSamyPath);
        } catch (PolicyException e) {
            e.printStackTrace();
        }
    }

    /**
     * AntiSamy过滤数据
     * @param taintedHTML 需要进行过滤的数据
     * @return 返回过滤后的数据
     * */
    private String xssClean( String taintedHTML){
        try{
            // 使用AntiSamy进行过滤
            AntiSamy antiSamy = new AntiSamy();
            CleanResults cr = antiSamy.scan( taintedHTML, policy);
            taintedHTML = cr.getCleanHTML();
        }catch( ScanException e) {
            e.printStackTrace();
        }catch( PolicyException e) {
            e.printStackTrace();
        }
        return taintedHTML;
    }

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name){
        String[] values = super.getParameterValues(name);
        if ( values == null){
            return null;
        }
        int len = values.length;
        String[] newArray = new String[len];
        for (int j = 0; j < len; j++){
            System.out.println("Antisamy过滤清理，清理之前的参数值：" + values[j]);
            // 过滤清理
            newArray[j] = xssClean(values[j]);
            System.out.println("Antisamy过滤清理，清理之后的参数值：" + newArray[j]);
        }
        return newArray;
    }
}
```

第十步：为了使上面定义的过滤器生效，需要创建配置类，用于初始化过滤器对象

```java
package com.woldier.config;

import cn.itcast.filter.XssFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AntiSamyConfiguration {
    /**
     * 配置跨站攻击过滤器
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistration = 
            new FilterRegistrationBean(new XssFilter());
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setOrder(1);

        return filterRegistration;
    }
}
```

启动项目，页面输入非法数据，可以看到非法数据被清理掉了。

注意：当前我们在进行请求参数过滤时只是在包装类的getParameterValues方法中进行了处理，真实项目中可能用户提交的数据在请求头中，也可能用户提交的是json数据，所以如果考虑所有情况，我们可以在包装类中的多个方法中都进行清理处理即可，如下：

```java
package cn.itcast.wrapper;

import org.owasp.validator.html.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

public class XssRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 策略文件 需要将要使用的策略文件放到项目资源文件路径下
     * */
    private static String antiSamyPath = XssRequestWrapper.class.getClassLoader()
            .getResource( "antisamy-ebay.xml").getFile();

    public static  Policy policy = null;
    static {
        // 指定策略文件
        try {
            policy = Policy.getInstance(antiSamyPath);
        } catch (PolicyException e) {
            e.printStackTrace();
        }
    }

    /**
     * AntiSamy过滤数据
     * @param taintedHTML 需要进行过滤的数据
     * @return 返回过滤后的数据
     * */
    private String xssClean( String taintedHTML){
        try{
            // 使用AntiSamy进行过滤
            AntiSamy antiSamy = new AntiSamy();
            CleanResults cr = antiSamy.scan( taintedHTML, policy);
            taintedHTML = cr.getCleanHTML();
        }catch( ScanException e) {
            e.printStackTrace();
        }catch( PolicyException e) {
            e.printStackTrace();
        }
        return taintedHTML;
    }

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    @Override
    public String[] getParameterValues(String name){
        String[] values = super.getParameterValues(name);
        if ( values == null){
            return null;
        }
        int len = values.length;
        String[] newArray = new String[len];
        for (int j = 0; j < len; j++){
            // 过滤清理
            newArray[j] = xssClean(values[j]);
        }
        return newArray;
    }

    @Override
    public String getParameter(String paramString) {
        String str = super.getParameter(paramString);
        if (str == null) {
            return null;
        }
        return xssClean(str);
    }


    @Override
    public String getHeader(String paramString) {
        String str = super.getHeader(paramString);
        if (str == null) {
            return null;
        }
        return xssClean(str);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> requestMap = super.getParameterMap();
        for (Map.Entry<String, String[]> me : requestMap.entrySet()) {
            String[] values = me.getValue();
            for (int i = 0; i < values.length; i++) {
                values[i] = xssClean(values[i]);
            }
        }
        return requestMap;
    }
}
```

## 2. wd-tools-xss使用

wd-tools-xss的实现和我们上面的入门案例是一致的，底层也是基于AntiSamy对输入参数进行检验和清理，确保输入符合应用规范。

为了方便使用，wd-tools-xss已经定义为了starter，其他应用只需要导入其maven坐标，不需要额外进行任何配置就可以使用。

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
    <groupId>com.itheima</groupId>
    <artifactId>myXssApp</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>com.woldier</groupId>
            <artifactId>wd-tools-xss</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

第二步：创建XSSController

```java
package com.woldier.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/xss")
public class XSSController {
    @GetMapping("/get")
    public String get(String text){
        return "处理之后的文本内容为：" + text;
    }
}
```

第三步：创建启动类

```java
package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XssApplication {
    public static void main(String[] args) {
        SpringApplication.run(XssApplication.class,args);
    }
}
```

启动项目，访问如下地址：\
localhost:9000/xss/get?text=1 
没问题
localhost:9000/xss/get?text=</script> 被拦截


## ps： 可以做的改进
目前 com.woldier.auth.xss.XssAuthConfiguration类里面 配置的排除地址为写死的 我们可能需要拓展他
目前的思路是 导入属性类进行设置


```java
    /**
     * 配置跨站攻击过滤器
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        //TODO 想想这里如何扩展 通过属性类增加更多的排除列表配置

        FilterRegistrationBean filterRegistration = new FilterRegistrationBean(new XssFilter());
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setOrder(1);

        Map<String, String> initParameters = new HashMap<>(2);

        /**
         * 排除列表
         */
        String excludes = new StringJoiner(",")
                .add("/favicon.ico")
                .add("/doc.html")
                .add("/swagger-ui.html")
                .add("/csrf")
                .add("/webjars/*")
                .add("/v2/*")
                .add("/swagger-resources/*")
                .add("/resources/*")
                .add("/static/*")
                .add("/public/*")
                .add("/classpath:*")
                .add("/actuator/*")
                .toString();
        initParameters.put("excludes", excludes);
        initParameters.put("isIncludeRichText", "true");

        filterRegistration.setInitParameters(initParameters); /*参数加入filter中*/

        return filterRegistration;
    }
```
