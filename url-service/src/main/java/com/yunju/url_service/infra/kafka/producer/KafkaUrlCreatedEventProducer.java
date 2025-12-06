package com.yunju.url_service.infra.kafka.producer;

import com.yunju.url_service.global.event.dto.ShortUrlCreatedEvent;
import com.yunju.url_service.global.event.producer.UrlCreatedEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUrlCreatedEventProducer implements UrlCreatedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "shorturl.url-created";

    @Override
    public void send(ShortUrlCreatedEvent event) {

        try {
            kafkaTemplate.send(TOPIC, event.getShortKey(), event);
            log.info("[PRODUCE] url.created → shortKey={}", event.getShortKey());

        } catch (Exception ex) {
            log.error("[FAILED PRODUCE] url.created 이벤트 발행 실패: {}", ex.getMessage(), ex);
        }
    }
}
