package com.yunju.stats_service.domain.stats.repository;

import com.yunju.stats_service.domain.stats.model.entity.UrlDailyDeviceClicks;
import com.yunju.stats_service.domain.stats.model.entity.UrlDeviceClicks;
import com.yunju.stats_service.domain.stats.model.id.UrlDeviceClicksId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UrlDeviceClicksRepository extends JpaRepository<UrlDeviceClicks, UrlDeviceClicksId> {

    List<UrlDeviceClicks> findByIdShortKey(String shortKey);

    List<UrlDeviceClicks> findByIdDeviceType(String deviceType);

    @Query("SELECT d FROM UrlDailyDeviceClicks d " +
            "WHERE d.id.date = :date AND d.id.deviceType = :device " +
            "ORDER BY d.clicks DESC")
    List<UrlDailyDeviceClicks> findTopByDevice(
            @Param("date") LocalDate date,
            @Param("device") String deviceType,
            Pageable pageable
    );
}
