
package com.yunju.redirect_service.infra.kafka.consumer;

import com.yunju.redirect_service.domain.redirect.service.UrlCreatedEventHandler;
import com.yunju.redirect_service.global.event.consumer.UrlCreatedEventConsumer;
import com.yunju.redirect_service.global.event.dto.ShortUrlCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUrlCreatedEventConsumer implements UrlCreatedEventConsumer {

    private final UrlCreatedEventHandler urlCreatedEventHandler;

    @KafkaListener(
            topics = "shorturl.url-created",
            groupId = "redirect-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ShortUrlCreatedEvent event) {

        log.info("[CONSUMER] url-created received → {}", event.getShortKey());

        urlCreatedEventHandler.handle(event);
    }
}
