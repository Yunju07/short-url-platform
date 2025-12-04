package com.yunju.shorturl_app.domain.shortUrl.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class ShortUrlCacheRepository {
    private static final String KEY_PREFIX = "shorturl:";
    private static final long EXPIRED_BUFFER_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String shortKey, ShortUrlCacheValue value) {
        long nowEpoch = Instant.now().getEpochSecond();
        long ttlSeconds = value.getExpireAt() - nowEpoch;

        if (ttlSeconds <= 0) {
            ttlSeconds = 0;
        }
        ttlSeconds += EXPIRED_BUFFER_SECONDS;

        String key = KEY_PREFIX + shortKey;

        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate
                    .opsForValue()
                    .set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            // 로깅만 하고, 캐시 실패는 서비스 동작에 영향을 주지 않도록 함
            // log.warn("Failed to serialize ShortUrlCacheValue", e);
        }
    }

    public Optional<ShortUrlCacheValue> findByShortKey(String shortKey) {
        String key = KEY_PREFIX + shortKey;
        String json = stringRedisTemplate.opsForValue().get(key);

        if (json == null) {
            return Optional.empty();
        }

        try {
            ShortUrlCacheValue value =
                    objectMapper.readValue(json, ShortUrlCacheValue.class);
            return Optional.of(value);
        } catch (JsonProcessingException e) {
            delete(shortKey);
            return Optional.empty();
        }
    }

    public void delete(String shortKey) {
        String key = KEY_PREFIX + shortKey;
        stringRedisTemplate.delete(key);
    }
}
