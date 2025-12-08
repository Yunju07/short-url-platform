package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.entity.UrlDailyDeviceClicks;
import com.yunju.stats_service.domain.stats.model.id.UrlDailyDeviceClicksId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UrlDailyDeviceClicksRepository extends JpaRepository<UrlDailyDeviceClicks, UrlDailyDeviceClicksId> {

    List<UrlDailyDeviceClicks> findByIdShortKey(String shortKey);

    @Query("SELECT d FROM UrlDailyDeviceClicks d " +
            "WHERE d.id.date = :date AND d.id.deviceType = :device " +
            "ORDER BY d.clicks DESC")
    List<UrlDailyDeviceClicks> findTopByDevice(
            @Param("date") LocalDate date,
            @Param("device") String deviceType,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(d.clicks), 0) " +
            "FROM UrlDailyDeviceClicks d " +
            "WHERE d.id.date = :date AND d.id.deviceType = :deviceType")
    long sumByDevice(
            @Param("date") LocalDate date,
            @Param("deviceType") String deviceType
    );
}
