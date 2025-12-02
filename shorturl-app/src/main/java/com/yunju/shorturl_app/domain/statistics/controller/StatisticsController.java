package com.yunju.shorturl_app.domain.statistics.controller;

import com.yunju.shorturl_app.domain.statistics.dto.ShortUrlStatisticsResponse;
import com.yunju.shorturl_app.domain.statistics.dto.TopStatisticsResponse;
import com.yunju.shorturl_app.domain.statistics.service.StatisticsService;
import com.yunju.shorturl_app.global.apiPayload.ApiResponse;
import com.yunju.shorturl_app.global.apiPayload.code.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
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
