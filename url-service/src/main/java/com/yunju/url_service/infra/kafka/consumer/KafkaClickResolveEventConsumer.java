package com.yunju.url_service.infra.kafka.consumer;

import com.yunju.url_service.domain.shorturl.repository.ShortUrlRepository;
import com.yunju.url_service.global.event.consumer.ClickResolveEventConsumer;
import com.yunju.url_service.global.event.dto.ClickResolveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class KafkaClickResolveEventConsumer implements ClickResolveEventConsumer {

    private final ShortUrlRepository shortUrlRepository;

    @KafkaListener(
            topics = "shorturl.click-resolve",
            groupId = "url-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ClickResolveEvent event) {

        log.info("[CONSUMER] click.resolve 수신 → {}", event.getShortKey());

        shortUrlRepository.findByShortKey(event.getShortKey())
                .ifPresent(url -> {
                    url.increaseClick(event.getClickedAt());
                    log.info("[CONSUMER] increaseClick 적용 완료 → {}, count={}",
                            url.getShortKey(), url.getTotalClicks());
                });
    }
}
