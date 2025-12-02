package com.yunju.shorturl_app.domain.statistics.repository;

import com.yunju.shorturl_app.domain.statistics.dto.ByDateDto;
import com.yunju.shorturl_app.domain.statistics.dto.ByDeviceDto;
import com.yunju.shorturl_app.domain.statistics.dto.ByReferrerDto;
import com.yunju.shorturl_app.domain.statistics.model.ShortUrlClickLog;
import com.yunju.shorturl_app.global.enums.DeviceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsQueryRepository extends JpaRepository<ShortUrlClickLog, Long> {


    @Query("""
        SELECT new com.yunju.shorturl_app.domain.statistics.dto.ByDateDto(
            DATE(l.clickedAt),
            COUNT(l)
        )
        FROM ShortUrlClickLog l
        WHERE l.shortUrl.id = :shortUrlId
        GROUP BY DATE(l.clickedAt)
        ORDER BY DATE(l.clickedAt)
    """)
    List<ByDateDto> countByDate(Long shortUrlId);

    @Query("""
        SELECT new com.yunju.shorturl_app.domain.statistics.dto.ByDeviceDto(
            l.deviceType,
            COUNT(l)
        )
        FROM ShortUrlClickLog l
        WHERE l.shortUrl.id = :shortUrlId
        GROUP BY l.deviceType
    """)
    List<ByDeviceDto> countByDevice(Long shortUrlId);

    @Query("""
        SELECT new com.yunju.shorturl_app.domain.statistics.dto.ByReferrerDto(
            COALESCE(l.referrer, 'direct'),
            COUNT(l)
        )
        FROM ShortUrlClickLog l
        WHERE l.shortUrl.id = :shortUrlId
        GROUP BY COALESCE(l.referrer, 'direct')
        ORDER BY COUNT(l) DESC
    """)
    List<ByReferrerDto> countByReferrer(Long shortUrlId);

    @Query("""
        SELECT l.shortUrl.id, l.shortUrl.shortKey, l.shortUrl.shortUrl,
               l.shortUrl.originalUrl, COUNT(l)
        FROM ShortUrlClickLog l
        WHERE DATE(l.clickedAt) = :date
        GROUP BY l.shortUrl.id, l.shortUrl.shortKey, l.shortUrl.shortUrl, l.shortUrl.originalUrl
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTopUrlsByDate(LocalDate date, Pageable pageable);

    @Query("""
        SELECT COALESCE(l.referrer, 'direct'), COUNT(l)
        FROM ShortUrlClickLog l
        WHERE DATE(l.clickedAt) = :date
        GROUP BY COALESCE(l.referrer, 'direct')
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTopReferrersByDate(LocalDate date, Pageable pageable);

    @Query("""
        SELECT l.deviceType, COUNT(l)
        FROM ShortUrlClickLog l
        WHERE DATE(l.clickedAt) = :date
        GROUP BY l.deviceType
    """)
    List<Object[]> findTotalClicksByDevice(LocalDate date);

    @Query("""
        SELECT l.shortUrl.id, l.shortUrl.shortKey, l.shortUrl.shortUrl,
               l.shortUrl.originalUrl, COUNT(l)
        FROM ShortUrlClickLog l
        WHERE DATE(l.clickedAt) = :date AND l.deviceType = :deviceType
        GROUP BY l.shortUrl.id, l.shortUrl.shortKey, l.shortUrl.shortUrl, l.shortUrl.originalUrl
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTopUrlsByDevice(LocalDate date, DeviceType deviceType, Pageable pageable);

}
