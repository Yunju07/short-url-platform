package com.yunju.stats_service.domain.stats.dto.response;

import com.yunju.stats_service.domain.stats.dto.components.TopByDeviceDto;
import com.yunju.stats_service.domain.stats.dto.components.TopReferrerDto;
import com.yunju.stats_service.domain.stats.dto.components.TopUrlDto;

import java.time.LocalDate;
import java.util.List;

public record DailyTopStatsResponse(
        LocalDate date,
        List<TopUrlDto> topUrls,
        List<TopReferrerDto> topReferrers,
        List<TopByDeviceDto> topByDevice,
        StatsMetadataResponse metadata
) {}
