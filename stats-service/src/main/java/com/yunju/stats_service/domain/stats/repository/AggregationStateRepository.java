package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.entity.AggregationState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AggregationStateRepository extends JpaRepository<AggregationState, Integer> {
}
