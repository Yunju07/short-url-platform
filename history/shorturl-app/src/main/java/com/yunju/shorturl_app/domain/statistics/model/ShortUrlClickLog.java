package com.yunju.shorturl_app.domain.statistics.model;

import com.yunju.shorturl_app.domain.shortUrl.model.ShortUrl;
import com.yunju.shorturl_app.global.enums.DeviceType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column(length = 500)
    private String referrer;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceType deviceType;

    public static ShortUrlClickLog create(ShortUrl shortUrl, String referrer, String userAgent, DeviceType deviceType, LocalDateTime clickTime) {
        return ShortUrlClickLog.builder()
                .shortUrl(shortUrl)
                .clickedAt(clickTime)
                .referrer(referrer)
                .userAgent(userAgent)
                .deviceType(deviceType)
                .build();
    }
}
