package com.yunju.stats_service.domain.stats.service;

import com.yunju.common.enums.DeviceType;
import com.yunju.stats_service.domain.stats.dto.*;
import com.yunju.stats_service.domain.stats.repository.StatisticsQueryRepository;
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

    private final StatisticsQueryRepository statisticsQueryRepository;


    public ShortUrlStatisticsResponse getStatistics(String shortKey) {

        var dateList = statisticsQueryRepository.countByDate(shortKey);
        var deviceList = statisticsQueryRepository.countByDevice(shortKey);
        var referrerList = statisticsQueryRepository.countByReferrer(shortKey);

        long totalClicks = dateList.stream()
                .mapToLong(ByDateDto::clicks)
                .sum();

        return new ShortUrlStatisticsResponse(
                shortKey,
                totalClicks,
                dateList,
                deviceList,
                referrerList
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
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (Long) row[3]
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
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (Long) row[3]
            ));
        }

        return result;
    }
}
