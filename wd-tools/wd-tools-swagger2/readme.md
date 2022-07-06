# 使用指南
访问地址 ： {baseurl}:{port}/doc.html
## 1.导入坐标

```xml

    <dependency>
        <groupId>com.woldier</groupId>
        <artifactId>wd-tools-swagger2</artifactId>.
        <version>1.0-SNAPSHOT</version>
    </dependency>
```
## 2. 修改配置文件

### 2.1 单分组
```yaml
 swagger:
    enabled: true #是否启用
    #----------单分组配置----------
    title: "在线文档"   #标题
    group: ""   #自定义组名
    description: "在线文档" #描述
    version: "1.0"   #版本
    contact: #new Contact()   #联系人
      name: "woldier"
      url: "www.woldier.com"
      email: "1098582358@qq.com"
    basePackage: "com.woldier.swagger.controller"   #swagger会解析的包路径
    basePath:   #swagger会解析的url规则
    excludePath :  #swagger会排除解析的url规则
```
### 2.2 多分组
```yaml
wd-auth:
  swagger:
    docket:
      user:
        title: "用户"
        basePackage: com.woldier.swagger.controller.user
      menu:
        title: "菜单模块"
        basePackage: com.woldier.swagger.controller.menu
```

## 3. 文档标注样例
### 3.1实体类
```java
package com.woldier.swagger.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "用户实体" ,description = "用户实体desc")
public class User {

    @ApiModelProperty(value = "主键")
    private int id;
    @ApiModelProperty(value = "名字")
    private String name;
    @ApiModelProperty(value = "年龄")
    private int age;
    @ApiModelProperty(value = "地址")
    private String address;
}
```
### 3.2 controller
```java
package com.woldier.swagger.controller.menu;
import com.woldier.swagger.entity.Menu;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/menu")
@Api(tags = "菜单控制器")
public class MenuController {
    @GetMapping("/getMenus")
    @ApiOperation(value = "查询所有菜单", notes = "查询所有菜单信息")
    public List<Menu> getMenus(){
        Menu menu = new Menu();
        menu.setId(100);
        menu.setName("itcast");
        List<Menu> list = new ArrayList<>();
        list.add(menu);
        return list;
    }
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNum", value = "页码", 
                         required = true, type = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", 
                         required = true, type = "Integer"),
    })
    @ApiOperation(value = "分页查询菜单信息")
    @GetMapping(value = "page/{pageNum}/{pageSize}")
    public String findByPage(@PathVariable Integer pageNum,
                             @PathVariable Integer pageSize) {
        return "OK";
    }
}
```