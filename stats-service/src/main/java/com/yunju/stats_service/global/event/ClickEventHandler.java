package com.yunju.stats_service.global.event;

import com.yunju.stats_service.domain.stats.service.ShortUrlClickLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventHandler {

    private final ShortUrlClickLogService clickLogService;

    @Transactional
    public void handle(ShortUrlClickedEvent event) {
        log.info("[HANDLER] Processing click event → shortKey={}, referrer={}, userAgent={}",
                event.getShortKey(), event.getReferrer(), event.getUserAgent());


        clickLogService.recordClick(
                event.getShortKey(),
                event.getShortUrl(),
                event.getOriginalUrl(),
                event.getUserAgent(),
                event.getReferrer(),
                event.getClickedAt()
        );
    }
}
