package com.yunju.stats_service.domain.stats.service;

import com.yunju.common.enums.DeviceType;
import com.yunju.common.util.DeviceTypeParser;
import com.yunju.stats_service.domain.stats.model.entity.ShortUrlClickLog;
import com.yunju.stats_service.domain.stats.repository.ShortUrlClickLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortUrlClickLogService {

    private final ShortUrlClickLogRepository clickLogRepository;

    public void recordClick(String shortKey, String shortUrl, String originalUrl, String userAgent, String referer, LocalDateTime clickTime) {
        DeviceType deviceType = DeviceTypeParser.parse(userAgent);

        ShortUrlClickLog log = ShortUrlClickLog.create(
                shortKey,
                shortUrl,
                originalUrl,
                referer,
                userAgent,
                deviceType,
                clickTime
        );

        clickLogRepository.save(log);
    }

}
