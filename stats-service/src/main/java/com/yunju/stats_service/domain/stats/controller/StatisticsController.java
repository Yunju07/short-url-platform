package com.yunju.stats_service.domain.stats.controller;

import com.yunju.stats_service.domain.stats.dto.ShortUrlStatisticsResponse;
import com.yunju.stats_service.domain.stats.dto.TopStatisticsResponse;
import com.yunju.stats_service.domain.stats.service.StatisticsService;
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

    private final StatisticsService statisticsService;

    @GetMapping("/urls/{shortKey}")
    public ResponseEntity<ApiResponse<ShortUrlStatisticsResponse>> getUrlStatistics(
            @PathVariable String shortKey
    ) {
        ShortUrlStatisticsResponse response = statisticsService.getStatistics(shortKey);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, response));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<TopStatisticsResponse>>getTopN(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit
    ) {
        TopStatisticsResponse response = statisticsService.getTopStatistics(date, limit);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, response));
    }

}
