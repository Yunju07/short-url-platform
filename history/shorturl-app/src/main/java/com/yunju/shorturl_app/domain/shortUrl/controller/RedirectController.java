package com.yunju.shorturl_app.domain.shortUrl.controller;

import com.yunju.shorturl_app.domain.shortUrl.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final ShortUrlService shortUrlService;

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortKey,
            HttpServletRequest request
    ) {
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        String originalUrl = shortUrlService.handleRedirect(
                shortKey,
                userAgent,
                referrer
        );

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

}