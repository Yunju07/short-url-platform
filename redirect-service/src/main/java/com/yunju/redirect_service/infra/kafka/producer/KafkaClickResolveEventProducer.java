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
        ClickResolveEvent event = ClickResolveEvent.builder()
            .shortKey(shortKey)
            .clickedAt(clickedAt)
            .build();

            kafkaTemplate.send(TOPIC, event.getShortKey(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("[PRODUCER FAIL] click.resolve shortKey={}, cause={}", shortKey, ex.getMessage());
                            return;
                        }

                        log.debug("[PRODUCE OK] click.resolve partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    });
        }
}
