package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.entity.UrlDailyClicks;
import com.yunju.stats_service.domain.stats.model.id.UrlDailyClicksId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UrlDailyClicksRepository extends JpaRepository<UrlDailyClicks, UrlDailyClicksId> {

    List<UrlDailyClicks> findByIdShortKey(String shortKey);

    @Query("""

            SELECT d 
        FROM UrlDailyClicks d
        WHERE d.id.date = :date
        ORDER BY d.totalClicks DESC
        """)
    List<UrlDailyClicks> findByDateOrderByClicksDesc(
            @Param("date") LocalDate date,
            Pageable pageable
    );

    // findByIdShortKeyOrderByIdDateAsc 메서드 추가
    List<UrlDailyClicks> findByIdShortKeyOrderByIdDateAsc(String shortKey);

}
