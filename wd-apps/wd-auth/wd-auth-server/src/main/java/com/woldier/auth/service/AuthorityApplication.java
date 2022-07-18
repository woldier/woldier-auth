package com.woldier.auth.service;

import com.woldier.auth.auth.server.EnableAuthServer;
import com.woldier.auth.user.annotation.EnableLoginArgResolver;
import com.woldier.auth.validator.config.EnableFormValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAuthServer /*jwt*/
@EnableFeignClients(value = {
        "com.woldier.auth",
})
@Slf4j
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableLoginArgResolver /*参数解析器*/
@EnableFormValidator /*表单校验*/

public class  AuthorityApplication {
    public static void main(String[] args) throws UnknownHostException {
       // SpringApplication.run(AuthorityApplication.class, args);
        ConfigurableApplicationContext run = SpringApplication.run(AuthorityApplication.class, args);
        /**
         * 打印knife4j url
         */
        ConfigurableEnvironment environment = run.getEnvironment();
        String port = environment.getProperty("server.port");
        String appName = environment.getProperty("spring.application.name");
        byte[] address = InetAddress.getLocalHost().getAddress();

        log.info("==============================================================");
        log.info("应用{}启动成功,对于的swagger文档地址是http://{}:{}/doc.html",appName,address,port);
        log.info("==============================================================");

    }


}
