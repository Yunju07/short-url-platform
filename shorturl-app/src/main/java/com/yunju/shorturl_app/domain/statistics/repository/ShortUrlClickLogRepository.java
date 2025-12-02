package com.yunju.shorturl_app.domain.statistics.repository;

import com.yunju.shorturl_app.domain.statistics.model.ShortUrlClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlClickLogRepository extends JpaRepository<ShortUrlClickLog, Long> { }
