package com.yunju.shorturl_app.domain.statistics.dto;

import com.yunju.shorturl_app.global.enums.DeviceType;
import java.util.List;

public record TopByDeviceDto(
        DeviceType deviceType,
        Long totalClicks,
        List<TopByDeviceUrlDto> topUrls
) {}
