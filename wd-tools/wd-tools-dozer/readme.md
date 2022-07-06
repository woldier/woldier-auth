# 1 dozer入门案例

第一步：创建maven工程dozer_demo并配置pom.xml文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/>
    </parent>
    <groupId>cn.itcast</groupId>
    <artifactId>dozer_demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>com.github.dozermapper</groupId>
            <artifactId>dozer-spring-boot-starter</artifactId>
            <version>6.5.0</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
```

第二步：创建UserDTO和UserEntity

```java
package com.woldier.dto;
import lombok.Data;
@Data
public class UserDTO {
    private String userId;
    private String userName;
    private int userAge;
    private String address;
    private String birthday;
}
```

```java
package com.woldier.entity;
import lombok.Data;
import java.util.Date;
@Data
public class UserEntity {
    private String id;
    private String name;
    private int age;
    private String address;
    private Date birthday;
}
```

第三步：在resources/dozer/目录下创建dozer的全局配置文件global.dozer.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mappings xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns="http://dozermapper.github.io/schema/bean-mapping"
          xsi:schemaLocation="http://dozermapper.github.io/schema/bean-mapping 
                              http://dozermapper.github.io/schema/bean-mapping.xsd">
    <!--
    全局配置:
    <date-format>表示日期格式
     -->
    <configuration>
        <date-format>yyyy-MM-dd</date-format>
    </configuration>
</mappings>
```

注：全局配置文件名称可以任意

第四步：在resources/dozer/目录下创建dozer的映射文件biz.dozer.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mappings xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns="http://dozermapper.github.io/schema/bean-mapping"
          xsi:schemaLocation="http://dozermapper.github.io/schema/bean-mapping
                             http://dozermapper.github.io/schema/bean-mapping.xsd">
    <!--描述两个类中属性的对应关系，对于两个类中同名的属性可以不映射-->
    <mapping date-format="yyyy-MM-dd">
        <class-a>com.woldier.entity.UserEntity</class-a>
        <class-b>com.woldier.dto.UserDTO</class-b>
        <field>
            <a>id</a>
            <b>userId</b>
        </field>
        <field>
            <a>name</a>
            <b>userName</b>
        </field>
        <field>
            <a>age</a>
            <b>userAge</b>
        </field>
    </mapping>
    <!--
    可以使用map-id指定映射的标识，在程序中通过此标识来确定使用当前这个映射关系
    -->
    <mapping date-format="yyyy-MM-dd" map-id="user">
        <class-a>com.woldier.entity.UserEntity</class-a>
        <class-b>com.woldier.dto.UserDTO</class-b>
        <field>
            <a>id</a>
            <b>userId</b>
        </field>
        <field>
            <a>name</a>
            <b>userName</b>
        </field>
        <field>
            <a>age</a>
            <b>userAge</b>
        </field>
    </mapping>
</mappings>
```

注：映射文件名称可以任意

第五步：编写application.yml文件

```yaml
dozer:
  mappingFiles:
    - classpath:dozer/global.dozer.xml
    - classpath:dozer/biz.dozer.xml
```

第六步：编写启动类DozerApp

```java
package com.woldier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DozerApp {
    public static void main(String[] args) {
        SpringApplication.run(DozerApp.class,args);
    }
}
```

第七步：编写单元测试DozerTest

```java
package com.woldier.test;

import com.github.dozermapper.core.DozerBeanMapper;
import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.github.dozermapper.core.metadata.MappingMetadata;
import com.woldier.DozerApp;
import com.woldier.dto.UserDTO;
import com.woldier.entity.UserEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DozerApp.class)
public class DozerTest {
    @Autowired
    private Mapper mapper;
    @Test
    public void testDozer1(){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId("100");
        userDTO.setUserName("itcast");
        userDTO.setUserAge(20);
        userDTO.setAddress("bj");
        userDTO.setBirthday("2010-11-20");

        UserEntity user = mapper.map(userDTO, UserEntity.class);
        System.out.println(user);
    }

    @Test
    public void testDozer2(){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId("100");
        userDTO.setUserName("itcast");
        userDTO.setUserAge(20);
        userDTO.setAddress("bj");
        userDTO.setBirthday("2010-11-20");

        UserEntity user = new UserEntity();
        user.setId("200");
        System.out.println(user);
        mapper.map(userDTO,user);
        System.out.println(user);
    }

    @Test
    public void testDozer3(){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId("100");
        userDTO.setUserName("itcast");
        userDTO.setUserAge(20);
        userDTO.setAddress("bj");

        UserEntity user = new UserEntity();
        System.out.println(user);
        mapper.map(userDTO,user,"user"); // 指定映射id
        System.out.println(user);
    }
}
```

# 2 wd-tools-dozer使用

在wd-tools-dozer模块中为了进一步简化操作，封装了一个工具类DozerUtils，其内部使用的就是Mapper对象进行的操作。并且按照Spring Boot starter的规范编写/resources/META-INF/spring.factories文件，内容如下：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    com.woldier.auth.dozer.DozerAutoConfiguration
```

在配置类DozerAutoConfiguration中完成DozerUtils对象的创建，这样其他的程序如果需要使用dozer进行对象转换，只需要引入这个模块的maven坐标并且提供对应的映射文件就可以在程序中直接注入DozerUtils对象进行操作了。

**具体使用过程：**

第一步：创建maven工程myDozerApp并配置pom.xml

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
    <artifactId>myDozerApp</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <!--引入我们自己定义的dozer基础模块-->
        <dependency>
            <groupId>com.woldier</groupId>
            <artifactId>wd-tools-dozer</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

第二步：创建UserEntity和UserDTO

```java
package com.itheima.entity;

import lombok.Data;

@Data
public class UserEntity {
    private Integer id;
    private String name;
    private int age;
}
```

```java
package com.itheima.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String name;
    private int age;
}
```

第三步：创建UserController

```java
package com.itheima.controller;

import com.itheima.dto.UserDTO;
import com.itheima.entity.UserEntity;
import com.itheima.pinda.dozer.DozerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private DozerUtils dozerUtils; //在pd-tools-dozer中已经完成了自动配置，可以直接注入

    @GetMapping("/mapper")
    public UserEntity mapper(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(10);
        userDTO.setName("woldier");
        userDTO.setAge(20);

        UserEntity userEntity = dozerUtils.map(userDTO, UserEntity.class);
        return userEntity;
    }
}
```

第四步：创建application.yml

```yaml
server:
  port: 8080
```

第五步：创建启动类

```java
package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyDozerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyDozerApplication.class,args);
    }
}
```

启动项目，访问地址：http://localhost:8080/user/mapper

```json
{
  "id": 10,
  "name": "woldier",
  "age": 20
}
```
注意：由于当前我们创建的UserEntity和UserDTO中的属性完全一致，所以并没有提供映射文件，如果这两个类中的属性存在不一致的情况，需要创建映射文件进行映射，并且还需要在application.yml中配置映射文件的位置，例如：

    映射文件前面有介绍 这里不在赘述
```yaml
dozer:
  mappingFiles:
    - classpath:dozer/biz.dozer.xml  #指定dozer的映射文件位置
```

