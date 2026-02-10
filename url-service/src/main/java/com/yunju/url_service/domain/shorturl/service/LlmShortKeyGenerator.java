package com.yunju.url_service.domain.shorturl.service;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import com.yunju.url_service.infra.shortkey.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
@Slf4j
public class LlmShortKeyGenerator implements ShortKeyGenerator {

    private final LlmClient llmClient;
    private final ShortKeyProperties properties;
    private final RandomShortKeyGenerator fallbackGenerator;

    @Override
    public String generate(String originalUrl) {
        if (!properties.getLlm().isEnabled()) {
            log.info("[LLM DISABLED] fallback generator used");
            return fallbackGenerator.generate(originalUrl);
        }

        int attempts = Math.max(1, properties.getLlm().getMaxAttempts());
        log.info("[LLM START] model={}, attempts={}", properties.getLlm().getModel(), attempts);
        for (int i = 0; i < attempts; i++) {
            String prompt = buildPrompt(originalUrl, properties.getLength());
            Optional<String> response = llmClient.generateShortKey(prompt);
            if (response.isEmpty()) {
                log.warn("[LLM EMPTY] attempt={}", i + 1);
                continue;
            }

            Optional<String> candidate = sanitize(response.get(), properties.getLength());
            if (candidate.isPresent()) {
                log.info("[LLM OK] attempt={}", i + 1);
                return candidate.get();
            }
            log.warn("[LLM INVALID] attempt={}", i + 1);
        }

        log.warn("[LLM FALLBACK] all attempts failed");
        return fallbackGenerator.generate(originalUrl);
    }

    private String buildPrompt(String originalUrl, int length) {
        return "You generate a shortKey for a URL.\n" +
                "Requirements:\n" +
                "- lowercase letters only\n" +
                "- length: 5 to 7 characters\n" +
                "- focus on the main meaning (topic/action) from the URL path\n" +
                "- avoid random strings; prefer a meaningful keyword\n" +
                "- output ONLY the shortKey\n" +
                "\n" +
                "Examples:\n" +
                "URL: https://example.com/auth/login\n" +
                "shortKey: login\n" +
                "\n" +
                "URL: https://docs.example.com/payment/refund\n" +
                "shortKey: refund\n" +
                "\n" +
                "URL: " + originalUrl + "\n" +
                "shortKey:";
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
