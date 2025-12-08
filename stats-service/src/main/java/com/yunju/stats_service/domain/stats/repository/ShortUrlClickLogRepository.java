package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.entity.ShortUrlClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShortUrlClickLogRepository extends JpaRepository<ShortUrlClickLog, Long> {
    List<ShortUrlClickLog> findByClickedAtAfter(LocalDateTime time);

}
