package com.yunju.redirect_service.global.event.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrlCreatedEvent {
    private String shortKey;
    private String originalUrl;
    private Long expireAtEpochSec;
}