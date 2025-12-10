package com.yunju.shorturl_app.infra.kafka.producer;

import com.yunju.shorturl_app.global.event.ClickEventProducer;
import com.yunju.shorturl_app.global.event.ShortUrlClickedEvent;
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
    public void sendClickEvent(String shortKey, String userAgent, String referrer) {

        ShortUrlClickedEvent event = ShortUrlClickedEvent.builder()
                .shortKey(shortKey)
                .userAgent(userAgent)
                .referrer(referrer)
                .clickedAt(LocalDateTime.now())
                .build();

        //log.info("[PRODUCER] Sending click event → topic={}, key={}, event={}", TOPIC, shortKey, event);

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
