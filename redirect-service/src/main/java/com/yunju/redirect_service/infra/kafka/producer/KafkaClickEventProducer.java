package com.yunju.redirect_service.infra.kafka.producer;

import com.yunju.redirect_service.global.event.dto.ShortUrlClickedEvent;
import com.yunju.redirect_service.global.event.producer.ClickEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaClickEventProducer implements ClickEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "shorturl.click-log";

    @Override
    public void send(String shortKey, String userAgent, String referrer, LocalDateTime clickedAt) {

        ShortUrlClickedEvent event = ShortUrlClickedEvent.builder()
                .shortKey(shortKey)
                .userAgent(userAgent)
                .referrer(referrer)
                .clickedAt(clickedAt)
                .build();


        kafkaTemplate.send(TOPIC, shortKey, event)
//                .thenAccept(result ->
//                        log.info("[PRODUCER] Successfully sent → topic={}, partition={}, offset={}",
//                                result.getRecordMetadata().topic(),
//                                result.getRecordMetadata().partition(),
//                                result.getRecordMetadata().offset())
//                )
                .exceptionally(ex -> {
                    //log.error("[PRODUCER] Failed to send event → {}", ex.getMessage());
                    return null;
                });
    }
}
