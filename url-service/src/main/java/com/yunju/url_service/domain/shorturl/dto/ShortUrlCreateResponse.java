package com.yunju.url_service.domain.shorturl.dto;

import java.time.LocalDateTime;

public record ShortUrlCreateResponse(
        String shortKey,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {}
