package com.yunju.url_service.infra.shortkey.ai;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiShortKeyClientConfig {

    @Bean(name = "aiRestClient")
    public RestClient aiRestClient(ShortKeyProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = properties.getAi().getTimeoutSeconds() * 1000;
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
