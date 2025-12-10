package com.yunju.shorturl_app.domain.statistics.dto;

public record TopReferrerDto(
        int rank,
        String referrer,
        Long totalClicks
) {}
