
package com.yunju.redirect_service.infra.kafka.consumer;

import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCache;
import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCacheValue;
import com.yunju.redirect_service.global.event.consumer.UrlCreatedEventConsumer;
import com.yunju.redirect_service.global.event.dto.ShortUrlCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUrlCreatedEventConsumer implements UrlCreatedEventConsumer {

    private final ShortUrlCache shortUrlCache;

    @KafkaListener(
            topics = "shorturl.url-created",
            groupId = "redirect-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ShortUrlCreatedEvent event) {
        log.info("[CONSUMER] url-created received → {}", event.getShortKey());

        try{
            ShortUrlCacheValue value = ShortUrlCacheValue.create(
                    event.getOriginalUrl(),
                    event.getExpireAtEpochSec()
            );

            shortUrlCache.save(event.getShortKey(), value);

        } catch (Exception e) {
            log.error("[CACHE FAIL] event={}, message={}", event.getShortKey(), e.getMessage(), e);
        }

        log.info("[CACHE WARMUP] Cached successfully → {}", event.getShortKey());
    }
}
