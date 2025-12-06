package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.ShortUrlClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlClickLogRepository extends JpaRepository<ShortUrlClickLog, Long> { }
