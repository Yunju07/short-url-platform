package com.yunju.redirect_service.global.event.consumer;

import com.yunju.redirect_service.global.event.dto.ShortUrlCreatedEvent;

public interface UrlCreatedEventConsumer {

    void consume(ShortUrlCreatedEvent event);
}
