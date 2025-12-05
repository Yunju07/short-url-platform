package com.yunju.stats_service.domain.stats.repository;

import com.yunju.common.enums.DeviceType;
import com.yunju.stats_service.domain.stats.dto.ByDateDto;
import com.yunju.stats_service.domain.stats.dto.ByDeviceDto;
import com.yunju.stats_service.domain.stats.dto.ByReferrerDto;
import com.yunju.stats_service.domain.stats.model.ShortUrlClickLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsQueryRepository extends JpaRepository<ShortUrlClickLog, Long> {

    /**
     * 날짜별 클릭수 집계
     */
    @Query("""
        SELECT new com.yunju.stats_service.domain.stats.dto.ByDateDto(
            DATE(l.clickedAt),
            COUNT(l)
        )
        FROM ShortUrlClickLog l
        WHERE l.shortKey = :shortKey
        GROUP BY DATE(l.clickedAt)
        ORDER BY DATE(l.clickedAt)
    """)
    List<ByDateDto> countByDate(String shortKey);


    /**
     * 기기별 클릭수 집계
     */
    @Query("""
        SELECT new com.yunju.stats_service.domain.stats.dto.ByDeviceDto(
            l.deviceType,
            COUNT(l)
        )
        FROM ShortUrlClickLog l
        WHERE l.shortKey = :shortKey
        GROUP BY l.deviceType
    """)
    List<ByDeviceDto> countByDevice(String shortKey);


    /**
     * referrer 기준 집계
     */
    @Query("""
        SELECT new com.yunju.stats_service.domain.stats.dto.ByReferrerDto(
            COALESCE(l.referrer, 'direct'),
            COUNT(l)
        )
        FROM ShortUrlClickLog l
        WHERE l.shortKey = :shortKey
        GROUP BY COALESCE(l.referrer, 'direct')
        ORDER BY COUNT(l) DESC
    """)
    List<ByReferrerDto> countByReferrer(String shortKey);


    /**
     * 날짜 기준 Top URL 목록 조회
     */
    @Query("""
        SELECT l.shortKey,
               MAX(s.shortUrl),
               MAX(s.originalUrl),
               COUNT(l)
        FROM ShortUrlClickLog l
        LEFT JOIN ShortUrl s ON s.shortKey = l.shortKey
        WHERE DATE(l.clickedAt) = :date
        GROUP BY l.shortKey
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTopUrlsByDate(LocalDate date, Pageable pageable);


    /**
     * 날짜 기준 Top referrer 조회
     */
    @Query("""
        SELECT COALESCE(l.referrer, 'direct'),
               COUNT(l)
        FROM ShortUrlClickLog l
        WHERE DATE(l.clickedAt) = :date
        GROUP BY COALESCE(l.referrer, 'direct')
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTopReferrersByDate(LocalDate date, Pageable pageable);


    /**
     * 전체 device 통계
     */
    @Query("""
        SELECT l.deviceType,
               COUNT(l)
        FROM ShortUrlClickLog l
        WHERE DATE(l.clickedAt) = :date
        GROUP BY l.deviceType
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTotalClicksByDevice(LocalDate date);


    /**
     * device 별 top URL 조회
     */
    @Query("""
        SELECT l.shortKey,
               MAX(s.shortUrl),
               MAX(s.originalUrl),
               COUNT(l)
        FROM ShortUrlClickLog l
        LEFT JOIN ShortUrl s ON s.shortKey = l.shortKey
        WHERE DATE(l.clickedAt) = :date
          AND l.deviceType = :deviceType
        GROUP BY l.shortKey
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTopUrlsByDevice(LocalDate date, DeviceType deviceType, Pageable pageable);

}
