package com.yunju.url_service.domain.shorturl.service;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ShortKeyValidator {
    private final int length;
    private final Set<Character> allowedChars;

    public ShortKeyValidator(ShortKeyProperties properties) {
        this.length = properties.getLength();
        this.allowedChars = new HashSet<>();
        for (char c : properties.getAlphabet().toCharArray()) {
            allowedChars.add(c);
        }
    }

    public boolean isValid(String key) {
        if (key == null || key.length() != length) {
            return false;
        }

        for (int i = 0; i < key.length(); i++) {
            if (!allowedChars.contains(key.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
