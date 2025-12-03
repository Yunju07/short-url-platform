package com.yunju.shorturl_app.domain.shortUrl.service;

import com.yunju.shorturl_app.domain.shortUrl.model.ShortUrl;
import com.yunju.shorturl_app.domain.shortUrl.repository.ShortUrlRepository;
import com.yunju.shorturl_app.domain.statistics.service.ShortUrlClickLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortUrlClickAsyncHandler {

    private final ShortUrlClickLogService shortUrlClickLogService;
    private final ShortUrlRepository shortUrlRepository;

    @Async("clickExecutor")
    @Transactional
    public void handleClick(String shortKey, String userAgent, String referer) {

        ShortUrl shortUrl = shortUrlRepository.findByShortKey(shortKey)
                .orElse(null);

        if (shortUrl == null) return;

        LocalDateTime now = LocalDateTime.now();

        shortUrl.increaseClick(now);
        shortUrlClickLogService.recordClick(shortUrl, userAgent, referer, now);
    }
}
