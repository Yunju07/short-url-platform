package com.yunju.redirect_service.domain.redirect.service;

import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCache;
import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCacheValue;
import com.yunju.redirect_service.domain.redirect.model.ShortUrl;
import com.yunju.redirect_service.domain.redirect.repository.ShortUrlRepository;
import com.yunju.redirect_service.global.apiPayload.code.status.ErrorStatus;
import com.yunju.redirect_service.global.apiPayload.exception.CustomApiException;
import com.yunju.redirect_service.global.event.dto.ClickResolveEvent;
import com.yunju.redirect_service.global.event.producer.ClickEventProducer;
import com.yunju.redirect_service.global.event.producer.ClickResolveEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectService {

    private final ShortUrlCache shortUrlCache;
    private final ShortUrlRepository shortUrlRepository;
    private final ClickEventProducer clickEventProducer;
    private final ClickResolveEventProducer clickResolveEventProducer;

    public String handleRedirect(String shortKey, String userAgent, String referrer) {

        LocalDateTime clickTime = LocalDateTime.now();

        clickEventProducer.send(shortKey, userAgent, referrer, clickTime);

        String originalUrl = getOriginalUrl(shortKey);

        clickResolveEventProducer.send(shortKey, clickTime);

        return originalUrl;
    }


    private String getOriginalUrl(String shortKey) {
        Optional<ShortUrlCacheValue> cacheOpt = shortUrlCache.findByShortKey(shortKey);

        // 캐시 Hit
        if (cacheOpt.isPresent()) {
            ShortUrlCacheValue cache = cacheOpt.get();

            long now = Instant.now().getEpochSecond();
            if (cache.getExpireAt() <= now) {
                throw new CustomApiException(ErrorStatus.SHORT_URL_EXPIRED);
            }

            //log.info("[Cache HIT] shortKey={}, originalUrl={}", shortKey, cache.getOriginalUrl());
            return cache.getOriginalUrl();
        }

        ShortUrl shortUrl = shortUrlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomApiException(ErrorStatus.SHORT_URL_NOT_FOUND));

        if (shortUrl.getExpiredAt().isBefore(LocalDateTime.now())) {
            //log.warn("[Cache MISS] shortKey={}, reason=EXPIRED, fallback=DB", shortKey);
            throw new CustomApiException(ErrorStatus.SHORT_URL_EXPIRED);
        }

        log.info("[Cache MISS] shortKey={}, fallback=DB", shortKey);

        // warm cache
        cacheShortUrl(shortUrl);

        return shortUrl.getOriginalUrl();

    }

    private void cacheShortUrl(ShortUrl url) {
        ShortUrlCacheValue cacheValue = ShortUrlCacheValue.from(url);
        shortUrlCache.save(url.getShortKey(), cacheValue);
    }
}
