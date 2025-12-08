package com.yunju.stats_service.domain.stats.model.id;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UrlDailyReferrerClicksId implements Serializable {

    private String shortKey;
    private LocalDate date;
    private String referrer;
}
