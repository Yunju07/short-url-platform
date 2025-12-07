package com.yunju.stats_service.domain.stats.model;

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

    public void increase(int count) {
        this.clicks += count;
    }
}
