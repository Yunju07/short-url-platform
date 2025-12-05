package com.yunju.url_service.domain.shorturl.service;

import com.yunju.url_service.domain.shorturl.dto.ShortUrlCreateRequest;
import com.yunju.url_service.domain.shorturl.dto.ShortUrlCreateResponse;
import com.yunju.url_service.domain.shorturl.dto.ShortUrlDetailResponse;
import com.yunju.url_service.domain.shorturl.model.ShortUrl;
import com.yunju.url_service.domain.shorturl.repository.ShortUrlRepository;
import com.yunju.url_service.global.apiPayload.code.status.ErrorStatus;
import com.yunju.url_service.global.apiPayload.exception.CustomApiException;
import com.yunju.url_service.global.event.dto.ShortUrlCreatedEvent;
import com.yunju.url_service.global.event.producer.UrlCreatedEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UrlCreatedEventProducer urlCreatedEventProducer;

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
                .totalClicks((long) 0)
                .build();

        ShortUrl saved = shortUrlRepository.save(entity);
        publishUrlCreatedEvent(saved);  // url-created 이벤트 발행

        return new ShortUrlCreateResponse(
                shortKey,
                shortUrl,
                request.originalUrl(),
                now,
                expiredAt
        );
    }

    @Transactional(readOnly = true)
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

    private void publishUrlCreatedEvent(ShortUrl saved) {
        try {
            ShortUrlCreatedEvent event = ShortUrlCreatedEvent.builder()
                    .shortKey(saved.getShortKey())
                    .originalUrl(saved.getOriginalUrl())
                    .expireAtEpochSec(saved.getExpiredAt().toEpochSecond(java.time.ZoneOffset.UTC))
                    .build();

            urlCreatedEventProducer.send(event);

            log.info("[EVENT PUBLISHED] url.created → shortKey={}", saved.getShortKey());
        } catch (Exception e) {
            log.error("[EVENT FAILED] url.created 발행 실패: {}", saved.getShortKey(), e);
        }
    }
}