package com.yunju.stats_service.domain.stats.dto;

import com.yunju.common.enums.DeviceType;

public record ByDeviceDto(DeviceType deviceType, Long clicks) {}
