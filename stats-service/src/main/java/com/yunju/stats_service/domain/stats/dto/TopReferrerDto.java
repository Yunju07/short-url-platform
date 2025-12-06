package com.yunju.stats_service.domain.stats.dto;

public record TopReferrerDto(
        int rank,
        String referrer,
        Long totalClicks
) {}
