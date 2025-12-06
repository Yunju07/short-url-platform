package com.yunju.url_service.global.event.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClickResolveEvent {
    private String shortKey;
    private LocalDateTime clickedAt;
}
