package com.yunju.shorturl_app.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI shortUrlApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Short URL API")
                        .description("단축 URL 서비스 API 명세")
                        .version("v1.0.0")
                );
    }
}
