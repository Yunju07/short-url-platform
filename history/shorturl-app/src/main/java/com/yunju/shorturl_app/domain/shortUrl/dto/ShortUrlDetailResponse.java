package com.yunju.shorturl_app.domain.shortUrl.dto;

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
            Integer totalClicks,
            LocalDateTime lastClickedAt
    ) {}
}
