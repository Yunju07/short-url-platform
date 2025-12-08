package com.yunju.stats_service.domain.stats.dto.components;

import java.time.LocalDate;

public record ByDateDto(LocalDate date, long clicks) {}
