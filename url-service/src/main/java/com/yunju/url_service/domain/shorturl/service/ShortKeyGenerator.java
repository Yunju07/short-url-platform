package com.yunju.url_service.domain.shorturl.service;

public interface ShortKeyGenerator {
    String generate(String originalUrl);
}
