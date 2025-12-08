package com.yunju.stats_service.domain.stats.dto.response;

import com.yunju.stats_service.domain.stats.dto.components.ByDateDto;
import com.yunju.stats_service.domain.stats.dto.components.ByDeviceDto;
import com.yunju.stats_service.domain.stats.dto.components.ByReferrerDto;

import java.util.List;

public record UrlStatisticsResponse(
        String shortKey,
        String shortUrl,
        String originalUrl,
        long totalClicks,
        List<ByDateDto> byDate,
        List<ByDeviceDto> byDevice,
        List<ByReferrerDto> byReferrer,
        StatsMetadataResponse metadata
) {}
