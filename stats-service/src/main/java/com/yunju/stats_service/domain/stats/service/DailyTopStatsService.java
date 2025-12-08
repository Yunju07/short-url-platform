package com.yunju.stats_service.domain.stats.service;

import com.yunju.common.enums.DeviceType;
import com.yunju.stats_service.domain.stats.dto.components.TopByDeviceDto;
import com.yunju.stats_service.domain.stats.dto.components.TopReferrerDto;
import com.yunju.stats_service.domain.stats.dto.components.TopUrlDto;
import com.yunju.stats_service.domain.stats.dto.response.DailyTopStatsResponse;
import com.yunju.stats_service.domain.stats.dto.response.StatsMetadataResponse;
import com.yunju.stats_service.domain.stats.repository.UrlDailyClicksRepository;
import com.yunju.stats_service.domain.stats.repository.UrlDailyDeviceClicksRepository;
import com.yunju.stats_service.domain.stats.repository.UrlDailyReferrerClicksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyTopStatsService {

    private final UrlDailyClicksRepository dailyClicksRepository;
    private final UrlDailyDeviceClicksRepository dailyDeviceClicksRepository;
    private final UrlDailyReferrerClicksRepository dailyReferrerClicksRepository;
    private final StatsMetadataService metadataService;

    @Value("${stats.batch.interval-ms}")
    private long batchIntervalMs;

    public DailyTopStatsResponse getDailyTop(LocalDate date, int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        // Top URLs
        var topUrlEntities = dailyClicksRepository.findByDateOrderByClicksDesc(date, pageable);

        List<TopUrlDto> topUrls =
                topUrlEntities.stream()
                        .map(e -> new TopUrlDto(
                                topUrlEntities.indexOf(e) + 1,
                                e.getId().getShortKey(),
                                e.getShortUrl(),
                                e.getOriginalUrl(),
                                e.getTotalClicks()
                        )).toList();

        // Top Referrers
        var ref = dailyReferrerClicksRepository.findTopReferrersByDate(date, pageable);

        List<TopReferrerDto> topReferrers =
                ref.stream()
                        .map(r -> new TopReferrerDto(
                                ref.indexOf(r) + 1,
                                (String) r[0],
                                ((Number) r[1]).longValue()
                        )).toList();

        // TopByDevice
        List<TopByDeviceDto> topByDevice =
                Arrays.stream(DeviceType.values())
                        .filter(device -> device != DeviceType.UNKNOWN) // UNKNOWN 제외
                        .map(device -> buildDeviceSection(date, device.name(), pageable))
                        .toList();

        StatsMetadataResponse metadata = metadataService.getMetadata();

        return new DailyTopStatsResponse(
                date,
                topUrls,
                topReferrers,
                topByDevice,
                metadata
        );
    }

    private TopByDeviceDto buildDeviceSection(LocalDate date, String device, Pageable pageable) {

        long total = dailyDeviceClicksRepository.sumByDevice(date, device);

        var list = dailyDeviceClicksRepository.findTopByDevice(date, device, pageable);

        List<TopUrlDto> urls = list.stream()
                .map(e -> new TopUrlDto(
                        list.indexOf(e) + 1,
                        e.getId().getShortKey(),
                        e.getShortUrl(),
                        e.getOriginalUrl(),
                        e.getClicks()
                )).toList();

        return new TopByDeviceDto(device, total, urls);
    }

}
