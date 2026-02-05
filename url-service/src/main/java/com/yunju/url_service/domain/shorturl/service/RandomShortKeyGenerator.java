package com.yunju.url_service.domain.shorturl.service;

import com.yunju.url_service.infra.shortkey.ShortKeyProperties;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomShortKeyGenerator implements ShortKeyGenerator {

    private final int length;
    private final String alphabet;
    private final Random random = new Random();

    public RandomShortKeyGenerator(ShortKeyProperties properties) {
        this.length = properties.getLength();
        this.alphabet = properties.getAlphabet();
    }

    @Override
    public String generate(String originalUrl) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
