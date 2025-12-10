package com.yunju.shorturl_app.domain.shortUrl.cache;

import java.util.Optional;

public interface ShortUrlCache {

    Optional<ShortUrlCacheValue> findByShortKey(String shortKey);

    void save(String shortKey, ShortUrlCacheValue value);

    void delete(String shortKey);
}
