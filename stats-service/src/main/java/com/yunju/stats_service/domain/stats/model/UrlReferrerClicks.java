package com.yunju.stats_service.domain.stats.model;

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

    public void increase(int count) {
        this.clicks += count;
    }
}
