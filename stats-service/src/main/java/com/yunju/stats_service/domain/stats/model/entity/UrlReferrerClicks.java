package com.yunju.stats_service.domain.stats.model.entity;

import com.yunju.stats_service.domain.stats.model.id.UrlReferrerClicksId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "url_referrer_clicks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UrlReferrerClicks {

    @EmbeddedId
    private UrlReferrerClicksId id;

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
