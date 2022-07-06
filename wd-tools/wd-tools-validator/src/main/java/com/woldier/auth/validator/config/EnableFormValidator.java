package com.woldier.auth.validator.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 在启动类上添加该注解来启动表单验证功能
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ValidatorConfiguration.class) //使用了EnableFormValidator注解 就会导入ValidatorConfiguration配置类 就可以开启快速返回
public @interface EnableFormValidator {
}
