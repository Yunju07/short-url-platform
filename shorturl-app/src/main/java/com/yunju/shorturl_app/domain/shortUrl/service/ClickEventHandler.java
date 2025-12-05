package com.yunju.shorturl_app.domain.statistics.service;

import com.yunju.shorturl_app.domain.shortUrl.model.ShortUrl;
import com.yunju.shorturl_app.domain.shortUrl.repository.ShortUrlRepository;
import com.yunju.shorturl_app.global.event.ShortUrlClickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventHandler {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlClickLogService clickLogService;

    @Transactional
    public void handle(ShortUrlClickedEvent event) {
        log.info("[HANDLER] Processing click event → shortKey={}, referrer={}, userAgent={}",
                event.getShortKey(), event.getReferrer(), event.getUserAgent());

        ShortUrl shortUrl = shortUrlRepository.findByShortKey(event.getShortKey())
                .orElse(null);

        if (shortUrl == null) {
            log.warn("[ClickEventHandler] Unknown shortKey={}, skip", event.getShortKey());
            return;
        }

        clickLogService.recordClick(
                shortUrl,
                event.getUserAgent(),
                event.getReferrer(),
                event.getClickedAt()
        );

        shortUrl.increaseClick(event.getClickedAt());
    }
}
