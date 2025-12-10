package com.yunju.shorturl_app.domain.statistics.dto;

public record TopByDeviceUrlDto(
        int rank,
        String shortKey,
        String shortUrl,
        String originalUrl,
        Long clicksFromThisDevice
) {}
