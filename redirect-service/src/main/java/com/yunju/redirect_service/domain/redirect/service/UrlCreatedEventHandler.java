package com.yunju.redirect_service.domain.redirect.service;

import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCache;
import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCacheValue;
import com.yunju.redirect_service.domain.redirect.model.UrlDocument;
import com.yunju.redirect_service.domain.redirect.repository.UrlReadRepository;
import com.yunju.redirect_service.global.event.dto.ShortUrlCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UrlCreatedEventHandler {

    private final ShortUrlCache shortUrlCache;
    private final UrlReadRepository urlReadRepository;

    public void handle(ShortUrlCreatedEvent event) {

        log.info("[HANDLER] Handling url-create event → {}", event.getShortKey());

        // 1) Mongo 저장
        UrlDocument doc = new UrlDocument(
                event.getShortKey(),
                event.getOriginalUrl(),
                event.getExpireAtEpochSec()
        );

        urlReadRepository.save(doc);

        // 2) Redis 캐시 warm-up
        ShortUrlCacheValue cacheValue =
                ShortUrlCacheValue.create(event.getOriginalUrl(), event.getExpireAtEpochSec());

        shortUrlCache.save(event.getShortKey(), cacheValue);

        log.info("[HANDLER DONE] doc+cache saved → {}", event.getShortKey());
    }
}
