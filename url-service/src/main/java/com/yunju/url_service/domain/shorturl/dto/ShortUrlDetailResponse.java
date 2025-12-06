package com.yunju.url_service.domain.shorturl.dto;

import java.time.LocalDateTime;

public record ShortUrlDetailResponse(
        String shortKey,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt,
        LocalDateTime expiredAt,
        ClickSummary clickSummary
) {
    public record ClickSummary(
            Long totalClicks,
            LocalDateTime lastClickedAt
    ) {}
}

