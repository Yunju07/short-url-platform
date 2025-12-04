package com.yunju.shorturl_app.infra.kafka.consumer;

import com.yunju.shorturl_app.domain.statistics.service.ClickEventHandler;
import com.yunju.shorturl_app.global.event.ShortUrlClickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaClickEventConsumer {

    private final ClickEventHandler clickEventHandler;

    @RetryableTopic(
        attempts = "3",                    // 총 3번 재시도
        backoff = @Backoff(delay = 1000) // 1초 간격
    )
    @KafkaListener(
        topics = "shorturl.click-log",
        groupId = "shorturl-click-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ShortUrlClickedEvent event) {
        clickEventHandler.handle(event);
    }
}
