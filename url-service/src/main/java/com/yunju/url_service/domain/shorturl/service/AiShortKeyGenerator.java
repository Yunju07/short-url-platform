package com.yunju.url_service.domain.shorturl.service;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import com.yunju.url_service.infra.shortkey.ai.AiShortKeyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
@Slf4j
public class AiShortKeyGenerator implements ShortKeyGenerator {

    private final AiShortKeyClient aiShortKeyClient;
    private final ShortKeyProperties properties;
    private final RandomShortKeyGenerator fallbackGenerator;

    @Override
    public String generate(String originalUrl) {
        if (!properties.getAi().isEnabled()) {
            log.info("[AI DISABLED] fallback generator used");
            return fallbackGenerator.generate(originalUrl);
        }

        int attempts = Math.max(1, properties.getAi().getMaxAttempts());
        int minLength = properties.getMinLength();
        int maxLength = properties.getMaxLength();

        for (int i = 0; i < attempts; i++) {
            Optional<String> response =
                    aiShortKeyClient.generateShortKey(originalUrl, minLength, maxLength);
            if (response.isEmpty()) {
                continue;
            }

            Optional<String> candidate = sanitize(response.get(), minLength, maxLength);
            if (candidate.isPresent()) {
                return candidate.get();
            }
        }

        log.warn("[AI FALLBACK] all attempts failed");
        return fallbackGenerator.generate(originalUrl);
    }

    private Optional<String> sanitize(String raw, int minLength, int maxLength) {
        if (raw == null) {
            return Optional.empty();
        }

        String cleaned = raw.trim().toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        int len = cleaned.length();
        if (len < minLength || len > maxLength) {
            return Optional.empty();
        }

        return Optional.of(cleaned);
    }
}

