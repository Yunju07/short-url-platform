package com.yunju.redirect_service.infra.kafka.producer;

import com.yunju.redirect_service.global.event.dto.ClickResolveEvent;
import com.yunju.redirect_service.global.event.producer.ClickResolveEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClickResolveEventProducer implements ClickResolveEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "shorturl.click-resolve";

    @Override
    public void send(String shortKey, LocalDateTime clickedAt) {
        try {
            ClickResolveEvent event = ClickResolveEvent.builder()
                .shortKey(shortKey)
                .clickedAt(clickedAt)
                .build();

            kafkaTemplate.send(TOPIC, event.getShortKey(), event);
            log.info("[PRODUCE] click.resolve 발행 → {}", event.getShortKey());
        } catch (Exception ex) {
            log.error("[FAILED PRODUCE] click.resolve 실패: {}", ex.getMessage(), ex);
        }
    }
}
