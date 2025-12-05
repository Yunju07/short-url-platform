package com.yunju.stats_service.domain.stats.dto;

public record TopUrlDto(
        int rank,
        String shortKey,
        String shortUrl,
        String originalUrl,
        Long totalClicks
) {}
