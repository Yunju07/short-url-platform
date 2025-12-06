package com.yunju.url_service.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI urlApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Service API")
                        .description("단축 URL 생성 및 상세 조회 API 명세")
                        .version("v1.0.0")
                );
    }
}
