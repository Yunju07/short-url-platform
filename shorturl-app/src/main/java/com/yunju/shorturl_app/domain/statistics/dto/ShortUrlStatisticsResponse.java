package com.yunju.shorturl_app.domain.statistics.dto;

import java.util.List;

public record ShortUrlStatisticsResponse(
        String shortKey,
        Integer totalClicks,
        List<ByDateDto> byDate,
        List<ByDeviceDto> byDevice,
        List<ByReferrerDto> byReferrer
) {}
