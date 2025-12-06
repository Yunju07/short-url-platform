package com.yunju.redirect_service.domain.redirect.repository;

import com.yunju.redirect_service.domain.redirect.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortKey(String shortKey);
}
