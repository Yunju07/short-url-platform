package com.yunju.redirect_service.global.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClickResolveEvent {
    private String shortKey;
    private LocalDateTime clickedAt;
}
