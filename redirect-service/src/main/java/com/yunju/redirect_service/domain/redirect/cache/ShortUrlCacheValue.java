package com.yunju.redirect_service.domain.redirect.cache;

import com.yunju.redirect_service.domain.redirect.model.UrlDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlCacheValue {

    private String originalUrl;
    private long expiredAtEpochSec;  // 만료 시각 (epoch seconds)

    public static ShortUrlCacheValue from(UrlDocument doc) {

        return new ShortUrlCacheValue(
                doc.getOriginalUrl(),
                doc.getExpiredAtEpochSec()
        );
    }

    public static ShortUrlCacheValue create(String originalUrl, long expireAtEpochSec) {
        return new ShortUrlCacheValue(originalUrl, expireAtEpochSec);
    }
}
