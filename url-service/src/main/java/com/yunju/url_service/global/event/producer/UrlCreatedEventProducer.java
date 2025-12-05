package com.yunju.url_service.global.event.producer;

import com.yunju.url_service.global.event.dto.ShortUrlCreatedEvent;

public interface UrlCreatedEventProducer {

    void send(ShortUrlCreatedEvent event);

}
