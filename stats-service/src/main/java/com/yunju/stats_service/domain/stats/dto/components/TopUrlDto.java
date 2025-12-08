package com.yunju.stats_service.domain.stats.dto.components;

public record TopUrlDto(
        int rank,
        String shortKey,
        String shortUrl,
        String originalUrl,
        long totalClicks
) {}
