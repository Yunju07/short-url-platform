package com.yunju.redirect_service.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI redirectApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Redirect Service API")
                        .description("Redirect 처리 API 명세")
                        .version("v1.0.0")
                );
    }
}

