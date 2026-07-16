package com.careeros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.careeros.matching.application.MatchingProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@EnableConfigurationProperties(MatchingProperties.class)
@ConfigurationPropertiesScan
public class CareerOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerOsApplication.class, args);
    }
}
