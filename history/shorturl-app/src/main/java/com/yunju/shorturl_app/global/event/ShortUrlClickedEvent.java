package com.yunju.shorturl_app.global.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrlClickedEvent {

    private String shortKey;
    private String userAgent;
    private String referrer;
    private LocalDateTime clickedAt;
}
