package com.yunju.shorturl_app.infra.kafka.producer;

import com.yunju.shorturl_app.global.event.ClickEventProducer;
import com.yunju.shorturl_app.global.event.ShortUrlClickedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
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

        kafkaTemplate.send(TOPIC, shortKey, event);
    }
}
