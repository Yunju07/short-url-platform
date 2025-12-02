package com.yunju.shorturl_app.domain.statistics.service;

import com.yunju.shorturl_app.domain.shortUrl.model.ShortUrl;
import com.yunju.shorturl_app.domain.statistics.model.ShortUrlClickLog;
import com.yunju.shorturl_app.domain.statistics.repository.ShortUrlClickLogRepository;
import com.yunju.shorturl_app.domain.statistics.util.DeviceTypeParser;
import com.yunju.shorturl_app.global.enums.DeviceType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortUrlClickLogService {

    private final ShortUrlClickLogRepository clickLogRepository;

    @Async
    public void recordClick(ShortUrl shortUrl, String userAgent, String referer, LocalDateTime clickTime) {
        DeviceType deviceType = DeviceTypeParser.parse(userAgent);

        ShortUrlClickLog log = ShortUrlClickLog.create(
                shortUrl,
                referer,
                userAgent,
                deviceType,
                clickTime
        );

        clickLogRepository.save(log);
    }

}
