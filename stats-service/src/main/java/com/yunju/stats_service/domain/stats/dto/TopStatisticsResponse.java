package com.yunju.stats_service.domain.stats.dto;

import java.time.LocalDate;
import java.util.List;

public record TopStatisticsResponse(
        LocalDate date,
        List<TopUrlDto> topUrls,
        List<TopReferrerDto> topReferrers,
        List<TopByDeviceDto> topByDevice
) {}
