package com.careeros.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI careerOsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareerOS API")
                        .description("AI-powered job discovery and career management platform")
                        .version("v1")
                        .contact(new Contact().name("CareerOS"))
                        .license(new License().name("Proprietary")));
    }
}
