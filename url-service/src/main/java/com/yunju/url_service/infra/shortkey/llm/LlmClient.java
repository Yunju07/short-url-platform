package com.yunju.url_service.infra.shortkey.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LlmClient {

    private final RestClient restClient;
    private final ShortKeyProperties properties;

    public Optional<String> generateShortKey(String prompt) {
        try {
            LlmGenerateRequest request = new LlmGenerateRequest(
                    properties.getLlm().getModel(),
                    prompt,
                    false
            );

            LlmGenerateResponse response = restClient.post()
                    .uri(properties.getLlm().getBaseUrl() + "/api/generate")
                    .body(request)
                    .retrieve()
                    .body(LlmGenerateResponse.class);

            if (response == null || response.response() == null) {
                return Optional.empty();
            }

            return Optional.of(response.response());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public record LlmGenerateRequest(
            String model,
            String prompt,
            boolean stream
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LlmGenerateResponse(String response) {}
}
