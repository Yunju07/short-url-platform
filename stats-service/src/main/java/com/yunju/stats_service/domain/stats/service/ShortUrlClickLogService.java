package com.yunju.stats_service.domain.stats.service;

import com.yunju.common.enums.DeviceType;
import com.yunju.common.util.DeviceTypeParser;
import com.yunju.stats_service.domain.stats.model.ShortUrl;
import com.yunju.stats_service.domain.stats.model.ShortUrlClickLog;
import com.yunju.stats_service.domain.stats.repository.ShortUrlClickLogRepository;
import com.yunju.stats_service.domain.stats.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortUrlClickLogService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlClickLogRepository clickLogRepository;

    public void recordClick(String shortKey, String userAgent, String referer, LocalDateTime clickTime) {
        DeviceType deviceType = DeviceTypeParser.parse(userAgent);

        ShortUrl shortUrl = shortUrlRepository.findByShortKey(shortKey).orElse(null);

        ShortUrlClickLog log = ShortUrlClickLog.create(
                shortUrl,        // FK 설정 가능하면 연결
                shortKey,
                referer,
                userAgent,
                deviceType,
                clickTime
        );

        clickLogRepository.save(log);
    }

}
