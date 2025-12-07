package com.yunju.stats_service.domain.stats.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "url_daily_clicks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UrlDailyClicks {

    @EmbeddedId
    private UrlDailyClicksId id;

    @Column(nullable = false)
    private int totalClicks;

    public void increase(int count) {
        this.totalClicks += count;
    }
}