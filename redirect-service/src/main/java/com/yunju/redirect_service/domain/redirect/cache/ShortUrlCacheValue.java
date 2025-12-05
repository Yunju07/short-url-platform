package com.yunju.redirect_service.domain.redirect.cache;

import com.yunju.redirect_service.domain.redirect.model.ShortUrl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlCacheValue {

    private String originalUrl;
    private long expireAt;  // 만료 시각 (epoch seconds)

    public static ShortUrlCacheValue from(ShortUrl shortUrl) {
        long epochExpiredAt = shortUrl.getExpiredAt()
                .toEpochSecond(ZoneOffset.of("+09:00"));

        return new ShortUrlCacheValue(
                shortUrl.getOriginalUrl(),
                epochExpiredAt
        );
    }

    public static ShortUrlCacheValue create(String originalUrl, long expireAt) {
        return new ShortUrlCacheValue(originalUrl, expireAt);
    }
}
