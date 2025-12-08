package com.yunju.stats_service.domain.stats.model.entity;

import com.yunju.stats_service.domain.stats.model.id.UrlDailyClicksId;
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

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false)
    private String shortUrl;

    public void increase(int count) {
        this.totalClicks += count;
    }
}
