package io.github.mqportal.config;

import io.github.mqportal.cluster.service.MqAdminService;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RocketMqProperties.class)
public class RocketMqAdminConfig {

    @Bean
    public DefaultMQAdminExt defaultMQAdminExt(RocketMqProperties properties) {
        DefaultMQAdminExt admin = new DefaultMQAdminExt();
        admin.setNamesrvAddr(properties.getNamesrvAddr());
        admin.setInstanceName("mq-portal-admin");
        return admin;
    }

    @Bean
    public MqAdminService mqAdminService(DefaultMQAdminExt admin, RocketMqProperties properties) {
        MqAdminService service = new MqAdminService(admin);
        service.setTimeoutMillis(properties.getTimeoutMillis());
        return service;
    }
}
