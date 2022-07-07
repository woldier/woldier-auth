package com.woldier.auth.log;


import com.woldier.auth.log.aspect.SysLogAspect;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 日志自动配置
 * <p>
 * 启动条件：
 * 1，存在web环境
 * 2，配置文件中pinda.log.enabled=true
 * 3，配置文件中不存在：pinda.log.enabled 值
 *
 */
@EnableAsync //开启异步处理
@Configuration
@AllArgsConstructor
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "wd-auth.log.enabled", havingValue = "true", matchIfMissing = true) //检测 wd-auth.log.enabled 属性的值
public class LogAutoConfiguration {

    /**
     * SysLogAspect bean注入
     * */
    @Bean
    @ConditionalOnMissingBean
    public SysLogAspect sysLogAspect() {
        return new SysLogAspect();
    }
}
