package com.yunju.shorturl_app.domain.shortUrl.controller;

import com.yunju.shorturl_app.domain.shortUrl.dto.*;
import com.yunju.shorturl_app.domain.shortUrl.service.ShortUrlService;
import com.yunju.shorturl_app.global.apiPayload.ApiResponse;
import com.yunju.shorturl_app.global.apiPayload.code.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
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
