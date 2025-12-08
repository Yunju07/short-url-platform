package com.yunju.stats_service.domain.stats.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AggregationState {

    @Id
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime lastAggregatedAt;

    public static AggregationState init() {
        return new AggregationState(1, LocalDateTime.of(1970, 1, 1, 0, 0));
    }

    public void update(LocalDateTime time) {
        this.lastAggregatedAt = time;
    }
}
