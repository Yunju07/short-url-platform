package com.yunju.stats_service.domain.stats.dto.components;

import java.util.List;

public record TopByDeviceDto(
        String deviceType,
        long totalClicks,
        List<TopUrlDto> topUrls
) {}