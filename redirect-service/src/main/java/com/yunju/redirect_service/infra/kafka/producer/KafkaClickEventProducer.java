package com.yunju.redirect_service.infra.kafka.producer;

import com.yunju.redirect_service.global.event.dto.ShortUrlClickedEvent;
import com.yunju.redirect_service.global.event.producer.ClickLogEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaClickEventProducer implements ClickLogEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "shorturl.click-log";

    @Override
    public void send(String shortKey, String shortUrl, String originalUrl, String userAgent, String referrer, LocalDateTime clickedAt){

        ShortUrlClickedEvent event = ShortUrlClickedEvent.builder()
                .shortKey(shortKey)
                .shortUrl(shortUrl)
                .originalUrl(originalUrl)
                .userAgent(userAgent)
                .referrer(referrer)
                .clickedAt(clickedAt)
                .build();


        kafkaTemplate.send(TOPIC, shortKey, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("[PRODUCER FAIL] shortKey={}, cause={}", shortKey, ex.getMessage());
                        return;
                    }

                    log.debug("[PRODUCE OK] shortKey={}, partition={}, offset={}",
                            shortKey,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
