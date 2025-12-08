package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.entity.UrlDailyReferrerClicks;
import com.yunju.stats_service.domain.stats.model.id.UrlDailyReferrerClicksId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UrlDailyReferrerClicksRepository
        extends JpaRepository<UrlDailyReferrerClicks, UrlDailyReferrerClicksId> {

    List<UrlDailyReferrerClicks> findByIdShortKey(String shortKey);

    @Query("SELECT r.id.referrer AS referrer, SUM(r.clicks) AS total " +
            "FROM UrlDailyReferrerClicks r " +
            "WHERE r.id.date = :date " +
            "GROUP BY r.id.referrer " +
            "ORDER BY total DESC")
    List<Object[]> findTopReferrersByDate(
            @Param("date") LocalDate date,
            Pageable pageable
    );
}
