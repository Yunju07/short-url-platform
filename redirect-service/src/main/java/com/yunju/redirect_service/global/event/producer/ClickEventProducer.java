package com.yunju.redirect_service.global.event.producer;

import java.time.LocalDateTime;

public interface ClickEventProducer {

    void send(String shortKey, String shortUrl, String originalUrl, String userAgent, String referrer, LocalDateTime clickedAt);

}