package com.yunju.url_service.domain.shorturl.dto;

public record ShortUrlCreateRequest(
        String originalUrl,
        Long ttl
) {}
