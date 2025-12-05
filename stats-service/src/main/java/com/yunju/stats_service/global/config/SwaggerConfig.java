package com.yunju.stats_service.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI statsApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Statistics Service API")
                        .description("URL 통계/Top 조회 API 명세")
                        .version("v1.0.0")
                );
    }
}

