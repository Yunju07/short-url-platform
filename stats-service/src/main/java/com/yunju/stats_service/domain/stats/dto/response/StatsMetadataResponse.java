package com.yunju.stats_service.domain.stats.dto.response;

import java.time.LocalDateTime;

public record StatsMetadataResponse(
        LocalDateTime lastUpdatedAt,
        long batchIntervalMinutes
) {}