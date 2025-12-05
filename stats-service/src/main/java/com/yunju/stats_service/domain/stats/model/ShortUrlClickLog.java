package com.yunju.stats_service.domain.stats.model;

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

    /** ShortUrl 테이블 FK */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    private String shortKey;

    private LocalDateTime clickedAt;

    private String referrer;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    public static ShortUrlClickLog create(
            ShortUrl url,
            String shortKey,
            String referrer,
            String userAgent,
            DeviceType deviceType,
            LocalDateTime clickedAt
    ) {
        return ShortUrlClickLog.builder()
                .shortUrl(url)
                .shortKey(shortKey)
                .referrer(referrer)
                .userAgent(userAgent)
                .deviceType(deviceType)
                .clickedAt(clickedAt)
                .build();
    }
}
