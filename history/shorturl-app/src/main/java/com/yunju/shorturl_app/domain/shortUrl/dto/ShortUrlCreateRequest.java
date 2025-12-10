package com.yunju.shorturl_app.domain.shortUrl.dto;

public record ShortUrlCreateRequest(
        String originalUrl,
        Long ttl
) {}
