package io.github.mqportal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("io.github.mqportal")
public class MqPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqPortalApplication.class, args);
    }
}
