package com.yunju.stats_service.domain.stats.service;

import com.yunju.stats_service.domain.stats.dto.components.ByDateDto;
import com.yunju.stats_service.domain.stats.dto.components.ByDeviceDto;
import com.yunju.stats_service.domain.stats.dto.components.ByReferrerDto;
import com.yunju.stats_service.domain.stats.dto.response.StatsMetadataResponse;
import com.yunju.stats_service.domain.stats.dto.response.UrlStatisticsResponse;
import com.yunju.stats_service.domain.stats.repository.AggregationStateRepository;
import com.yunju.stats_service.domain.stats.repository.UrlDailyClicksRepository;
import com.yunju.stats_service.domain.stats.repository.UrlDailyDeviceClicksRepository;
import com.yunju.stats_service.domain.stats.repository.UrlDailyReferrerClicksRepository;
import com.yunju.stats_service.global.apiPayload.code.status.ErrorStatus;
import com.yunju.stats_service.global.apiPayload.exception.CustomApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlStatisticsService {

    private final UrlDailyClicksRepository dailyClicksRepository;
    private final UrlDailyDeviceClicksRepository dailyDeviceClicksRepository;
    private final UrlDailyReferrerClicksRepository dailyReferrerClicksRepository;
    private final StatsMetadataService metadataService;

    @Value("${stats.batch.interval-ms}")
    private long batchIntervalMs;

    public UrlStatisticsResponse getStatistics(String shortKey) {


        var byDateEntities = dailyClicksRepository
                .findByIdShortKeyOrderByIdDateAsc(shortKey);

        if (byDateEntities.isEmpty()) {
            throw new CustomApiException(ErrorStatus.STATS_NOT_FOUND);
        }

        List<ByDateDto> byDate = byDateEntities.stream()
                .map(e -> new ByDateDto(
                        e.getId().getDate(),
                        e.getTotalClicks()
                )).toList();

        long totalClicks = byDate.stream()
                .mapToLong(ByDateDto::clicks)
                .sum();


        var deviceEntities = dailyDeviceClicksRepository.findByIdShortKey(shortKey);

        List<ByDeviceDto> byDevice =
                deviceEntities.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getId().getDeviceType(),
                                Collectors.summingLong(e -> (long) e.getClicks())
                        ))
                        .entrySet().stream()
                        .map(entry -> new ByDeviceDto(entry.getKey(), entry.getValue()))
                        .toList();


        var referrerEntities = dailyReferrerClicksRepository.findByIdShortKey(shortKey);

        List<ByReferrerDto> byReferrer =
                referrerEntities.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getId().getReferrer(),
                                Collectors.summingLong(e -> (long) e.getClicks())
                        ))
                        .entrySet().stream()
                        .map(entry -> new ByReferrerDto(entry.getKey(), entry.getValue()))
                        .toList();


        StatsMetadataResponse metadata = metadataService.getMetadata();

        // 대표 shortUrl, originalUrl 가져오기
        String shortUrl = byDateEntities.get(0).getShortUrl();
        String originalUrl = byDateEntities.get(0).getOriginalUrl();

        return new UrlStatisticsResponse(
                shortKey,
                shortUrl,
                originalUrl,
                totalClicks,
                byDate,
                byDevice,
                byReferrer,
                metadata
        );
    }

}
