package com.yunju.redirect_service.domain.redirect.service;

import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCache;
import com.yunju.redirect_service.domain.redirect.cache.ShortUrlCacheValue;
import com.yunju.redirect_service.domain.redirect.model.ShortUrl;
import com.yunju.redirect_service.domain.redirect.model.UrlDocument;
import com.yunju.redirect_service.domain.redirect.repository.ShortUrlRepository;
import com.yunju.redirect_service.domain.redirect.repository.UrlDocumentRepository;
import com.yunju.redirect_service.global.apiPayload.code.status.ErrorStatus;
import com.yunju.redirect_service.global.apiPayload.exception.CustomApiException;
import com.yunju.redirect_service.global.event.producer.ClickLogEventProducer;
import com.yunju.redirect_service.global.event.producer.ClickResolveEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectService {

    private final ShortUrlCache shortUrlCache;
    private final ClickLogEventProducer clickLogEventProducer;
    private final ClickResolveEventProducer clickResolveEventProducer;
    private final UrlDocumentRepository urlDocumentRepository;
    private final ShortUrlRepository shortUrlRepository;

    @Value("${shorturl.domain}")
    private String shortUrlDomain;

    public String handleRedirect(String shortKey, String userAgent, String referrer) {

        LocalDateTime clickTime = LocalDateTime.now();

        try {
            String originalUrl = getOriginalUrl(shortKey);

            // 정상 클릭 로그
            clickLogEventProducer.send(
                    shortKey,
                    buildShortUrl(shortKey),
                    originalUrl,
                    userAgent,
                    referrer,
                    clickTime);

            clickResolveEventProducer.send(shortKey, clickTime);

            return originalUrl;

        } catch (CustomApiException e) {
            // 리다이렉트 실패 시에도 로그 기록
            log.info("[Redirect Fail] shortKey={} click-log save.", shortKey);
            clickLogEventProducer.send(
                    shortKey,
                    "",
                    "",
                    userAgent,
                    referrer,
                    clickTime
            );
            throw e;
        }
    }

    private String buildShortUrl(String shortKey) {
        if (shortUrlDomain.endsWith("/")) {
            return shortUrlDomain + shortKey;
        }
        return shortUrlDomain + "/" + shortKey;
    }

    private String getOriginalUrl(String shortKey) {

        // 1) 캐시 조회 시도
        Optional<ShortUrlCacheValue> cacheOpt = findFromCache(shortKey);
        if (cacheOpt.isPresent()) {
            ShortUrlCacheValue cache = cacheOpt.get();
            validateNotExpired(cache.getExpiredAtEpochSec());
            return cache.getOriginalUrl();
        }

        // 2) MongoDB 조회
        UrlDocument doc = findFromMongoOrFallback(shortKey);

        validateNotExpired(doc.getExpiredAtEpochSec());

        // 3) warm cache
        cacheShortUrl(doc);

        return doc.getOriginalUrl();
    }

    private Optional<ShortUrlCacheValue> findFromCache(String shortKey) {
        return shortUrlCache.findByShortKey(shortKey);
    }

    private UrlDocument findFromMongoOrFallback(String shortKey) {

        // 1) Mongo 조회
        Optional<UrlDocument> mongoOpt = findFromMongo(shortKey);
        if (mongoOpt.isPresent()) {
            log.info("[Cache MISS] shortKey={} → HIT in MongoDB", shortKey);
            return mongoOpt.get();
        }

        log.warn("[Mongo MISS] shortKey={} → fallback to MySQL", shortKey);

        // 2) MySQL 조회
        ShortUrl mysqlEntity = findFromMySql(shortKey);

        // 3) Mongo Self-Healing
        UrlDocument healed = healMongo(mysqlEntity);

        return healed;
    }

    private Optional<UrlDocument> findFromMongo(String shortKey) {
        return urlDocumentRepository.findById(shortKey);
    }

    private ShortUrl findFromMySql(String shortKey) {
        return shortUrlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomApiException(ErrorStatus.SHORT_URL_NOT_FOUND));
    }

    private void cacheShortUrl(UrlDocument doc) {
        ShortUrlCacheValue cacheValue = ShortUrlCacheValue.from(doc);
        shortUrlCache.save(doc.getId(), cacheValue);
    }

    private UrlDocument healMongo(ShortUrl mysql) {

        UrlDocument doc = new UrlDocument(
                mysql.getShortKey(),
                mysql.getOriginalUrl(),
                mysql.getExpiredAt().toEpochSecond(java.time.ZoneOffset.UTC)
        );

        urlDocumentRepository.save(doc);

        log.info("[Mongo HEAL] shortKey={} → Mongo upsert completed", mysql.getShortKey());

        return doc;
    }

    private void validateNotExpired(Long expiredAtEpochSec) {
        if (expiredAtEpochSec <= Instant.now().getEpochSecond()) {
            throw new CustomApiException(ErrorStatus.SHORT_URL_EXPIRED);
        }
    }
}
