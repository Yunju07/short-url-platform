package com.yunju.stats_service.domain.stats.dto.components;

public record TopByDeviceUrlDto(
        int rank,
        String shortKey,
        String shortUrl,
        String originalUrl,
        long clicksFromThisDevice
) {}
