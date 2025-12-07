package com.yunju.url_service.infra.kafka.consumer;

import com.yunju.url_service.global.event.consumer.ClickResolveEventConsumer;
import com.yunju.url_service.global.event.dto.ClickResolveEvent;
import com.yunju.url_service.infra.aggregation.ClickAggregationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClickResolveEventConsumer implements ClickResolveEventConsumer {

    private final ClickAggregationStore aggregationStore;

    @KafkaListener(
            topics = "shorturl.click-resolve",
            groupId = "url-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ClickResolveEvent event) {

        log.info("[CONSUMER] click.resolve 수신 → {}", event.getShortKey());

        aggregationStore.accumulate(event.getShortKey(), event.getClickedAt());

        log.debug("[CONSUME OK] buffering → {}", event.getShortKey());
    }
}
