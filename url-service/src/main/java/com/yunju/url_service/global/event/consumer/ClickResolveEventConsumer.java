package com.yunju.url_service.global.event.consumer;

import com.yunju.url_service.global.event.dto.ClickResolveEvent;

public interface ClickResolveEventConsumer {

    void consume(ClickResolveEvent event);
}
