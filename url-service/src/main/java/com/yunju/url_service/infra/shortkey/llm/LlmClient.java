package com.yunju.url_service.infra.shortkey.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmClient {

    private final RestClient restClient;
    private final ShortKeyProperties properties;
    private final ObjectMapper objectMapper;

    public Optional<String> generateShortKey(String prompt) {
        try {
            LlmGenerateRequest request = new LlmGenerateRequest(
                    properties.getLlm().getModel(),
                    prompt,
                    false
            );

            return restClient.post()
                    .uri(properties.getLlm().getBaseUrl() + "/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM)
                    .body(request)
                    .exchange((req, res) -> {
                        try (InputStream is = res.getBody()) {
                            if (is == null) {
                                return Optional.empty();
                            }

                            String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                            if (raw.isBlank()) {
                                return Optional.empty();
                            }

                            LlmGenerateResponse response =
                                    objectMapper.readValue(raw, LlmGenerateResponse.class);

                            if (response == null || response.response() == null || response.response().isBlank()) {
                                log.info("[LLM RAW] {}", truncate(raw, 500));
                                return Optional.empty();
                            }

                            return Optional.of(response.response());
                        }
                    });

        } catch (Exception e) {
            log.warn("[LLM ERROR] model={}, baseUrl={}, cause={}",
                    properties.getLlm().getModel(),
                    properties.getLlm().getBaseUrl(),
                    e.getMessage());
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

    private String truncate(String raw, int maxLen) {
        if (raw == null) {
            return "";
        }
        if (raw.length() <= maxLen) {
            return raw;
        }
        return raw.substring(0, maxLen) + "...";
    }
}
