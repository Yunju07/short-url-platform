package com.yunju.stats_service.domain.stats.dto;

import com.yunju.common.enums.DeviceType;

import java.util.List;

public record TopByDeviceDto(
        DeviceType deviceType,
        Long totalClicks,
        List<TopByDeviceUrlDto> topUrls
) {}
