package com.yunju.shorturl_app.domain.shortUrl.dto;

import java.time.LocalDateTime;

public record ShortUrlCreateResponse(
        String shortKey,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {}
