package com.yunju.stats_service.infra.kafka.consumer;

import com.yunju.stats_service.global.event.ClickEventHandler;
import com.yunju.stats_service.global.event.ShortUrlClickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaClickEventConsumer {

    private final ClickEventHandler clickEventHandler;

    @KafkaListener(
            topics = "shorturl.click-log",
            groupId = "stats-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ShortUrlClickedEvent event) {
        //log.info("[CONSUMER] Parsed event → {}", event);

        try {
            clickEventHandler.handle(event);
            //log.info("[CONSUMER] Event handled successfully → shortKey={}", event.getShortKey());
        } catch (Exception ex) {
            //log.error("[CONSUMER] Failed to process event → {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
