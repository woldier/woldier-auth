package com.woldier.auth.service.config;
import com.woldier.auth.databases.properties.DatabaseProperties;
import com.woldier.auth.databases.datasource.BaseMybatisConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableConfigurationProperties(DatabaseProperties.class)
public class ServiceMybatisConfiguration extends BaseMybatisConfiguration {

    public ServiceMybatisConfiguration(DatabaseProperties databaseProperties) {
        super(databaseProperties);
    }
}
