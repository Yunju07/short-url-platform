package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.entity.UrlReferrerClicks;
import com.yunju.stats_service.domain.stats.model.id.UrlReferrerClicksId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UrlReferrerClicksRepository extends JpaRepository<UrlReferrerClicks, UrlReferrerClicksId> {

    List<UrlReferrerClicks> findByIdShortKey(String shortKey);

    List<UrlReferrerClicks> findByIdReferrer(String referrer);

    @Query("SELECT r.id.referrer AS referrer, SUM(r.clicks) AS total " +
            "FROM UrlReferrerClicks r " +
            "GROUP BY r.id.referrer " +
            "ORDER BY total DESC")
    List<Object[]> findTopReferrers(Pageable pageable);
}
