package com.yunju.url_service.domain.shorturl.service;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import com.yunju.url_service.infra.shortkey.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
public class LlmShortKeyGenerator implements ShortKeyGenerator {

    private final LlmClient llmClient;
    private final ShortKeyProperties properties;
    private final RandomShortKeyGenerator fallbackGenerator;

    @Override
    public String generate(String originalUrl) {
        if (!properties.getLlm().isEnabled()) {
            return fallbackGenerator.generate(originalUrl);
        }

        int attempts = Math.max(1, properties.getLlm().getMaxAttempts());
        for (int i = 0; i < attempts; i++) {
            String prompt = buildPrompt(originalUrl, properties.getLength());
            Optional<String> response = llmClient.generateShortKey(prompt);
            if (response.isEmpty()) {
                continue;
            }

            Optional<String> candidate = sanitize(response.get(), properties.getLength());
            if (candidate.isPresent()) {
                return candidate.get();
            }
        }

        return fallbackGenerator.generate(originalUrl);
    }

    private String buildPrompt(String originalUrl, int length) {
        return "Generate a meaningful short key for a URL shortener. " +
                "Use only letters and digits (a-z, A-Z, 0-9). " +
                "Length must be exactly " + length + ". " +
                "Return only the key with no extra text. " +
                "URL: " + originalUrl;
    }

    private Optional<String> sanitize(String raw, int length) {
        if (raw == null) {
            return Optional.empty();
        }

        String cleaned = raw.trim()
                .replaceAll("[^a-zA-Z0-9]", "");

        if (cleaned.length() < length) {
            return Optional.empty();
        }

        if (cleaned.length() > length) {
            cleaned = cleaned.substring(0, length);
        }

        return Optional.of(cleaned);
    }
}
