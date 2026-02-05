package com.yunju.url_service.infra.shortkey.llm;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class LlmClientConfig {

    @Bean
    public RestClient llmRestClient(ShortKeyProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = properties.getLlm().getTimeoutSeconds() * 1000;
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
