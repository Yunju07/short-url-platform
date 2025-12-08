package com.yunju.stats_service.domain.stats.model.entity;

import com.yunju.stats_service.domain.stats.model.id.UrlDailyReferrerClicksId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "url_daily_referrer_clicks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UrlDailyReferrerClicks {

    @EmbeddedId
    private UrlDailyReferrerClicksId id;

    @Column(nullable = false)
    private int clicks;

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false)
    private String shortUrl;

    public void increase(int count) {
        this.clicks += count;
    }
}
