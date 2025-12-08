package com.yunju.redirect_service.global.event.dto;

import lombok.*;
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
