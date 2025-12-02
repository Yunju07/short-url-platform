package com.yunju.shorturl_app.domain.shortUrl.repository;

import com.yunju.shorturl_app.domain.shortUrl.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    @Query("""
        SELECT s FROM ShortUrl s 
        WHERE s.originalUrl = :originalUrl 
          AND s.expiredAt > CURRENT_TIMESTAMP()
    """)
    Optional<ShortUrl> findValidByOriginalUrl(@Param("originalUrl") String originalUrl);

    @Query("""
        SELECT s FROM ShortUrl s
        WHERE s.shortKey = :shortKey
          AND s.expiredAt > CURRENT_TIMESTAMP()
    """)
    Optional<ShortUrl> findValidByShortKey(@Param("shortKey") String shortKey);


    Optional<ShortUrl> findByShortKey(String shortKey);
}
