package com.yunju.stats_service.domain.stats.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UrlDailyDeviceClicksId implements Serializable {

    private String shortKey;
    private LocalDate date;
    private String deviceType;
}
