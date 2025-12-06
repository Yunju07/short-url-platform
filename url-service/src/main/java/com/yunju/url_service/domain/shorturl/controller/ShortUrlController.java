package com.yunju.url_service.domain.shorturl.controller;

import com.yunju.url_service.domain.shorturl.dto.ShortUrlCreateRequest;
import com.yunju.url_service.domain.shorturl.dto.ShortUrlCreateResponse;
import com.yunju.url_service.domain.shorturl.dto.ShortUrlDetailResponse;
import com.yunju.url_service.domain.shorturl.service.ShortUrlService;
import com.yunju.url_service.global.apiPayload.ApiResponse;
import com.yunju.url_service.global.apiPayload.code.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/urls")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShortUrlCreateResponse>> createShortUrl(
            @RequestBody ShortUrlCreateRequest request
    ) {

        ShortUrlCreateResponse response = shortUrlService.createShortUrl(request);

        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.CREATED, response));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<ShortUrlDetailResponse>> getDetail(
            @PathVariable("key") String key
    ) {
        ShortUrlDetailResponse response = shortUrlService.getShortUrlDetail(key);
        return ResponseEntity.ok(ApiResponse.of(SuccessStatus.OK, response));
    }
}
