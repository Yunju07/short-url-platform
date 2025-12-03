package com.yunju.shorturl_app.domain.shortUrl.service;

import com.yunju.shorturl_app.domain.shortUrl.cache.ShortUrlCacheRepository;
import com.yunju.shorturl_app.domain.shortUrl.cache.ShortUrlCacheValue;
import com.yunju.shorturl_app.domain.shortUrl.dto.ShortUrlCreateRequest;
import com.yunju.shorturl_app.domain.shortUrl.dto.ShortUrlCreateResponse;
import com.yunju.shorturl_app.domain.shortUrl.dto.ShortUrlDetailResponse;
import com.yunju.shorturl_app.domain.shortUrl.model.ShortUrl;
import com.yunju.shorturl_app.domain.shortUrl.repository.ShortUrlRepository;
import com.yunju.shorturl_app.domain.statistics.service.ShortUrlClickLogService;
import com.yunju.shorturl_app.global.apiPayload.code.status.ErrorStatus;
import com.yunju.shorturl_app.global.apiPayload.exception.CustomApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlCacheRepository shortUrlCacheRepository;
    private final ShortUrlClickLogService shortUrlClickLogService;

    private static final Long DEFAULT_TTL = 2592000L; // 30일
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final String SHORT_URL_DOMAIN = "https://short.example.com";

    public ShortUrlCreateResponse createShortUrl(ShortUrlCreateRequest request) {
        // 멱등성 체크 — 동일 originalUrl이면 기존 shortURL 반환
        Optional<ShortUrl> existing =
                shortUrlRepository.findValidByOriginalUrl(request.originalUrl());

        if (existing.isPresent()) {
            ShortUrl url = existing.get();
            cacheShortUrl(url);
            return new ShortUrlCreateResponse(
                    url.getShortKey(),
                    buildFullShortUrl(url.getShortKey()),
                    url.getOriginalUrl(),
                    url.getCreatedAt(),
                    url.getExpiredAt()
            );
        }

        long ttl = request.ttl() != null ? request.ttl() : DEFAULT_TTL;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusSeconds(ttl);

        String shortKey = generateShortKey();
        String shortUrl = buildFullShortUrl(shortKey);

        ShortUrl entity = ShortUrl.builder()
                .shortKey(shortKey)
                .shortUrl(shortUrl)
                .originalUrl(request.originalUrl())
                .createdAt(now)
                .expiredAt(expiredAt)
                .totalClicks(0)
                .build();

        ShortUrl saved = shortUrlRepository.save(entity);
        cacheShortUrl(saved);

        return new ShortUrlCreateResponse(
                shortKey,
                shortUrl,
                request.originalUrl(),
                now,
                expiredAt
        );
    }

    @Transactional
    public String handleRedirect(String shortKey, String userAgent, String referer) {

        String originalUrl = getOriginalUrl(shortKey);

        asyncHandleClick(shortKey, userAgent, referer);

        return originalUrl;
    }

    @Transactional(readOnly=true)
    public ShortUrlDetailResponse getShortUrlDetail(String shortKey) {

        ShortUrl shortUrl = shortUrlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomApiException(ErrorStatus.SHORT_URL_NOT_FOUND));

        return new ShortUrlDetailResponse(
                shortUrl.getShortKey(),
                shortUrl.getShortUrl(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiredAt(),
                new ShortUrlDetailResponse.ClickSummary(
                        shortUrl.getTotalClicks(),
                        shortUrl.getLastClickedAt()
                )
        );
    }

    private String generateShortKey() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(SHORT_KEY_LENGTH);

        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }

        return sb.toString();
    }

    private String buildFullShortUrl(String key) {
        return SHORT_URL_DOMAIN + "/" + key;
    }

    private void cacheShortUrl(ShortUrl url) {
        ShortUrlCacheValue cacheValue = ShortUrlCacheValue.from(url);
        shortUrlCacheRepository.save(url.getShortKey(), cacheValue);
    }

    private String getOriginalUrl(String shortKey) {
        Optional<ShortUrlCacheValue> cacheOpt = shortUrlCacheRepository.findByShortKey(shortKey);

        // 캐시 Hit
        if (cacheOpt.isPresent()) {
            ShortUrlCacheValue cache = cacheOpt.get();

            long now = Instant.now().getEpochSecond();
            if (cache.getExpireAt() <= now) {
                shortUrlCacheRepository.delete(shortKey);
                throw new CustomApiException(ErrorStatus.SHORT_URL_EXPIRED);
            }

            log.info("[Cache HIT] shortKey={}, originalUrl={}", shortKey, cache.getOriginalUrl());
            return cache.getOriginalUrl();
        }

        // 캐시 Miss → DB fallback
        ShortUrl shortUrl = shortUrlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomApiException(ErrorStatus.SHORT_URL_NOT_FOUND));

        if (shortUrl.getExpiredAt().isBefore(LocalDateTime.now())) {
            log.warn("[Cache MISS] shortKey={}, reason=EXPIRED, fallback=DB", shortKey);
            throw new CustomApiException(ErrorStatus.SHORT_URL_EXPIRED);
        }

        log.info("[Cache MISS] shortKey={}, fallback=DB", shortKey);

        // warm cache
        cacheShortUrl(shortUrl);

        return shortUrl.getOriginalUrl();
    }

    @Async
    @Transactional
    public void asyncHandleClick(String shortKey, String userAgent, String referer) {

        ShortUrl shortUrl = shortUrlRepository.findByShortKey(shortKey)
                .orElse(null);

        // TODO: 추후 에러 추적/보강 전략 필요
        if (shortUrl == null) return;

        LocalDateTime clickTime = LocalDateTime.now();

        shortUrl.increaseClick(clickTime);

        shortUrlClickLogService.recordClick(shortUrl, userAgent, referer, clickTime);
    }
}
