package com.yunju.stats_service.domain.stats.controller;

import com.yunju.stats_service.domain.stats.dto.response.UrlStatisticsResponse;
import com.yunju.stats_service.domain.stats.dto.response.DailyTopStatsResponse;
import com.yunju.stats_service.domain.stats.service.DailyTopStatsService;
import com.yunju.stats_service.domain.stats.service.UrlStatisticsService;
import com.yunju.stats_service.global.apiPayload.ApiResponse;
import com.yunju.stats_service.global.apiPayload.code.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/stats")
public class StatisticsController {

    private final UrlStatisticsService urlStatisticsService;
    private final DailyTopStatsService dailyTopStatsService;

    @GetMapping("/urls/{shortKey}")
    public ResponseEntity<ApiResponse<UrlStatisticsResponse>> getUrlStatistics(
            @PathVariable String shortKey
    ) {
        UrlStatisticsResponse response = urlStatisticsService.getStatistics(shortKey);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, response));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<DailyTopStatsResponse>> getTopN(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit
    ) {
        DailyTopStatsResponse response = dailyTopStatsService.getDailyTop(date, limit);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, response));
    }
}

