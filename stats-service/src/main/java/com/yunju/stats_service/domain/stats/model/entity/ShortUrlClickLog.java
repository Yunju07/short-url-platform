package com.yunju.stats_service.domain.stats.model.entity;

import com.yunju.common.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShortUrlClickLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortKey;

    private String shortUrl;

    private String originalUrl;

    private LocalDateTime clickedAt;

    private String referrer;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    public static ShortUrlClickLog create(
            String shortKey,
            String shortUrl,
            String originalUrl,
            String referrer,
            String userAgent,
            DeviceType deviceType,
            LocalDateTime clickedAt
    ) {
        return ShortUrlClickLog.builder()
                .shortKey(shortKey)
                .shortUrl(shortUrl)
                .originalUrl(originalUrl)
                .referrer(referrer)
                .userAgent(userAgent)
                .deviceType(deviceType)
                .clickedAt(clickedAt)
                .build();
    }
}
