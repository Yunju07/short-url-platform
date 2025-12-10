package com.yunju.shorturl_app.domain.statistics.service;

import com.yunju.shorturl_app.domain.shortUrl.model.ShortUrl;
import com.yunju.shorturl_app.domain.shortUrl.repository.ShortUrlRepository;
import com.yunju.shorturl_app.domain.statistics.dto.*;
import com.yunju.shorturl_app.domain.statistics.repository.StatisticsQueryRepository;
import com.yunju.shorturl_app.global.apiPayload.code.status.ErrorStatus;
import com.yunju.shorturl_app.global.apiPayload.exception.CustomApiException;
import com.yunju.shorturl_app.global.enums.DeviceType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final ShortUrlRepository shortUrlRepository;
    private final StatisticsQueryRepository statisticsQueryRepository;

    public ShortUrlStatisticsResponse getStatistics(String shortKey) {
        ShortUrl shortUrl = shortUrlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomApiException(ErrorStatus.SHORT_URL_NOT_FOUND));

        Long shortUrlId = shortUrl.getId();

        return new ShortUrlStatisticsResponse(
                shortUrl.getShortKey(),
                shortUrl.getTotalClicks(),
                statisticsQueryRepository.countByDate(shortUrlId),
                statisticsQueryRepository.countByDevice(shortUrlId),
                statisticsQueryRepository.countByReferrer(shortUrlId)
        );
    }

    public TopStatisticsResponse getTopStatistics(LocalDate date, int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        List<TopUrlDto> topUrls = buildTopUrls(date, pageable);
        List<TopReferrerDto> topReferrers = buildTopReferrers(date, pageable);
        List<TopByDeviceDto> topByDevice = buildTopByDevice(date, pageable);

        return new TopStatisticsResponse(
                date,
                topUrls,
                topReferrers,
                topByDevice
        );
    }

    private List<TopUrlDto> buildTopUrls(LocalDate date, Pageable pageable) {

        var rows = statisticsQueryRepository.findTopUrlsByDate(date, pageable);
        List<TopUrlDto> result = new ArrayList<>();

        int rank = 1;
        for (Object[] row : rows) {
            result.add(new TopUrlDto(
                    rank++,
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (Long) row[4]
            ));
        }

        return result;
    }

    private List<TopReferrerDto> buildTopReferrers(LocalDate date, Pageable pageable) {

        var rows = statisticsQueryRepository.findTopReferrersByDate(date, pageable);
        List<TopReferrerDto> result = new ArrayList<>();

        int rank = 1;
        for (Object[] row : rows) {
            result.add(new TopReferrerDto(
                    rank++,
                    (String) row[0],
                    (Long) row[1]
            ));
        }

        return result;
    }

    private List<TopByDeviceDto> buildTopByDevice(LocalDate date, Pageable pageable) {

        var deviceTotals = statisticsQueryRepository.findTotalClicksByDevice(date);
        List<TopByDeviceDto> result = new ArrayList<>();

        for (Object[] deviceRow : deviceTotals) {

            DeviceType deviceType = (DeviceType) deviceRow[0];
            Long totalClicks = (Long) deviceRow[1];

            List<TopByDeviceUrlDto> urls = buildTopUrlsByDevice(date, deviceType, pageable);

            result.add(new TopByDeviceDto(
                    deviceType,
                    totalClicks,
                    urls
            ));
        }

        return result;
    }

    private List<TopByDeviceUrlDto> buildTopUrlsByDevice(
            LocalDate date,
            DeviceType deviceType,
            Pageable pageable
    ) {

        var rows = statisticsQueryRepository.findTopUrlsByDevice(date, deviceType, pageable);
        List<TopByDeviceUrlDto> result = new ArrayList<>();

        int rank = 1;
        for (Object[] row : rows) {
            result.add(new TopByDeviceUrlDto(
                    rank++,
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (Long) row[4]
            ));
        }

        return result;
    }
}
