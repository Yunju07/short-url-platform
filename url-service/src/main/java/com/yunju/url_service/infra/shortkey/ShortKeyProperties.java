package com.yunju.url_service.infra.shortkey;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shortkey")
public class ShortKeyProperties {
    private int minLength = 6;
    private int maxLength = 8;
    // Policy: lowercase + [a-z0-9]
    private String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
    private int maxUniqueAttempts = 5;

    private Ai ai = new Ai();

    @Getter
    @Setter
    public static class Ai {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:8000";
        private int timeoutSeconds = 2;
        private int maxAttempts = 3;
    }
}
