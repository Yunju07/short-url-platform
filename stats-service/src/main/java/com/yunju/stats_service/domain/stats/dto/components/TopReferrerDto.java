package com.yunju.stats_service.domain.stats.dto.components;

public record TopReferrerDto(
        int rank,
        String referrer,
        long totalClicks
) {}
