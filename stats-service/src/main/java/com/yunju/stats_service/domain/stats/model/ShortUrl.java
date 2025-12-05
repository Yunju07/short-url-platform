package com.yunju.stats_service.domain.stats.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 50)
    private String shortKey;

    @Column(nullable = false, unique = true, length = 1000)
    private String shortUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private Long totalClicks;

    private LocalDateTime lastClickedAt;
}
