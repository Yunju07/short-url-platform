package com.yunju.redirect_service.domain.redirect.controller;

import com.yunju.redirect_service.domain.redirect.service.RedirectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final RedirectService redirectService;

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortKey,
            HttpServletRequest request
    ) {
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        String originalUrl = redirectService.handleRedirect(
                shortKey,
                userAgent,
                referrer
        );

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

}