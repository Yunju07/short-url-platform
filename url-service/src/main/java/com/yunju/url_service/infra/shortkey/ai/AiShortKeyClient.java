package com.yunju.url_service.infra.shortkey.ai;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiShortKeyClient {

    @Qualifier("aiRestClient")
    private final RestClient aiRestClient;
    private final ShortKeyProperties properties;

    public Optional<String> generateShortKey(String originalUrl, int minLength, int maxLength) {
        try {
            ShortKeyCreateRequest request =
                    new ShortKeyCreateRequest(originalUrl, minLength, maxLength, properties.getAlphabet());

            ShortKeyCreateResponse response = aiRestClient.post()
                    .uri(properties.getAi().getBaseUrl() + "/v1/shortkeys")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ShortKeyCreateResponse.class);

            if (response == null || response.shortKey() == null || response.shortKey().isBlank()) {
                return Optional.empty();
            }

            return Optional.of(response.shortKey());
        } catch (Exception e) {
            log.warn("[AI SERVICE ERROR] baseUrl={}, cause={}",
                    properties.getAi().getBaseUrl(),
                    e.getMessage());
            return Optional.empty();
        }
    }

    public record ShortKeyCreateRequest(
            String originalUrl,
            int minLength,
            int maxLength,
            String alphabet
    ) {}

    public record ShortKeyCreateResponse(String shortKey) {}
}
