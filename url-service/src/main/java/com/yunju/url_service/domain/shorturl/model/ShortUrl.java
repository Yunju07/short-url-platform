package com.yunju.url_service.domain.shorturl.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    // 클릭 증가 로직
    public void increaseClick(LocalDateTime clickedAt) {
        this.totalClicks += 1;
        this.lastClickedAt = clickedAt;
    }
}
