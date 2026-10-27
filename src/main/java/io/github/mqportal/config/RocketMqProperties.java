package io.github.mqportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqProperties {

    private String namesrvAddr = "localhost:9876";

    private long timeoutMillis = 3000;
}
