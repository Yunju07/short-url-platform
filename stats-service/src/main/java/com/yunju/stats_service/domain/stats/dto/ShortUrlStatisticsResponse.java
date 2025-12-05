package com.yunju.stats_service.domain.stats.dto;

import java.util.List;

public record ShortUrlStatisticsResponse(
        String shortKey,
        Long totalClicks,
        List<ByDateDto> byDate,
        List<ByDeviceDto> byDevice,
        List<ByReferrerDto> byReferrer
) {}
