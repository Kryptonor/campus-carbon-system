package com.carbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarbonApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarbonApplication.class, args);
    }
}
