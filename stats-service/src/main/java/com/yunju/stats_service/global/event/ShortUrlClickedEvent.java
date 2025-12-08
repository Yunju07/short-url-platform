package com.yunju.stats_service.global.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrlClickedEvent {
    private String shortKey;
    private String shortUrl;
    private String originalUrl;
    private String userAgent;
    private String referrer;
    private LocalDateTime clickedAt;
}
