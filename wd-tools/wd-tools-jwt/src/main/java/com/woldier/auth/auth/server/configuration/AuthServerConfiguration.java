package com.woldier.auth.auth.server.configuration;


import com.woldier.auth.auth.server.properties.AuthServerProperties;
import com.woldier.auth.auth.server.utils.JwtTokenServerUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 认证服务端配置
 * 此类并没有加入@configration注解 因此不会被spring管理
 *  其开启是通过@EnableAuthServer注解实现的
 */
@EnableConfigurationProperties(value = {
        AuthServerProperties.class,
})
public class AuthServerConfiguration {
    @Bean //注解为bean 通过容器注入AuthServerProperties  实现JwtTokenServerUtils的注入
    public JwtTokenServerUtils getJwtTokenServerUtils(AuthServerProperties authServerProperties) {
        /*通过构造方法将authServerProperties配置到实现JwtTokenServerUtils的注入中*/
        return new JwtTokenServerUtils(authServerProperties);
    }
}
