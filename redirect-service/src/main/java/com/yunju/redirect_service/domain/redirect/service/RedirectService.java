package com.yunju.redirect_service.domain.redirect.service;

import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCache;
import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCacheValue;
import com.yunju.redirect_service.domain.redirect.model.UrlDocument;
import com.yunju.redirect_service.domain.redirect.repository.UrlReadRepository;
import com.yunju.redirect_service.global.apiPayload.code.status.ErrorStatus;
import com.yunju.redirect_service.global.apiPayload.exception.CustomApiException;
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
    private final ClickEventProducer clickEventProducer;
    private final ClickResolveEventProducer clickResolveEventProducer;
    private final UrlReadRepository urlReadRepository;

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

            if (isExpired(cache.getExpireAt())) {
                throw new CustomApiException(ErrorStatus.SHORT_URL_EXPIRED);
            }

            return cache.getOriginalUrl();
        }


        UrlDocument doc = urlReadRepository.findById(shortKey)
                .orElseThrow(() -> new CustomApiException(ErrorStatus.SHORT_URL_NOT_FOUND));

        if (doc.isExpired()) {
            throw new CustomApiException(ErrorStatus.SHORT_URL_EXPIRED);
        }

        log.info("[Cache MISS] shortKey={}, fallback=DB", shortKey);

        // warm cache
        cacheShortUrl(doc);

        return doc.getOriginalUrl();

    }

    private void cacheShortUrl(UrlDocument doc) {
        ShortUrlCacheValue cacheValue = ShortUrlCacheValue.from(doc);
        shortUrlCache.save(doc.getId(), cacheValue);
    }

    private boolean isExpired(long expireAt) {
        return expireAt <= Instant.now().getEpochSecond();
    }
}
