package com.yunju.shorturl_app.domain.statistics.dto;

public record TopUrlDto(
        int rank,
        String shortKey,
        String shortUrl,
        String originalUrl,
        Long totalClicks
) {}
