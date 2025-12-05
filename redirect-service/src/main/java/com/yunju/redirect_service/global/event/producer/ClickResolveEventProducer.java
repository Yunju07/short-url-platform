package com.yunju.redirect_service.global.event.producer;

import java.time.LocalDateTime;

public interface ClickResolveEventProducer {

    void send(String shortKey, LocalDateTime clickedAt);
}
