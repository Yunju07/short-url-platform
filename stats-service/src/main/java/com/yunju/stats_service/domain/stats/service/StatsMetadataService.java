package com.yunju.stats_service.domain.stats.service;

import com.yunju.stats_service.domain.stats.dto.response.StatsMetadataResponse;
import com.yunju.stats_service.domain.stats.model.entity.AggregationState;
import com.yunju.stats_service.domain.stats.repository.AggregationStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsMetadataService {

    private final AggregationStateRepository aggregationStateRepository;

    @Value("${stats.batch.interval-ms}")
    private long batchIntervalMs;

    public StatsMetadataResponse getMetadata() {
        AggregationState state = aggregationStateRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("AggregationState not initialized"));

        return new StatsMetadataResponse(
                state.getLastAggregatedAt(),
                batchIntervalMs / 1000 / 60   // 분 단위 변환
        );
    }
}
