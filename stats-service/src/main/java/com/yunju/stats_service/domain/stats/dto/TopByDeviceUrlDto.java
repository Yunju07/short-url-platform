package com.yunju.stats_service.domain.stats.dto;

public record TopByDeviceUrlDto(
        int rank,
        String shortKey,
        String shortUrl,
        String originalUrl,
        Long clicksFromThisDevice
) {}
