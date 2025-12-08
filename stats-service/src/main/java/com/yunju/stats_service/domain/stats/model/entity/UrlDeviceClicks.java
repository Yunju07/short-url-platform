package com.yunju.stats_service.domain.stats.model.entity;

import com.yunju.stats_service.domain.stats.model.id.UrlDeviceClicksId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "url_device_clicks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UrlDeviceClicks {

    @EmbeddedId
    private UrlDeviceClicksId id;

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
