package com.yunju.shorturl_app.domain.statistics.dto;

import com.yunju.shorturl_app.global.enums.DeviceType;

public record ByDeviceDto(DeviceType deviceType, Long clicks) {}
