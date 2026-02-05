package com.yunju.url_service.infra.shortkey;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shortkey")
public class ShortKeyProperties {
    private int length = 6;
    private String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private int maxUniqueAttempts = 5;
    private Llm llm = new Llm();

    @Getter
    @Setter
    public static class Llm {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3.1";
        private int timeoutSeconds = 2;
        private int maxAttempts = 3;
    }
}
