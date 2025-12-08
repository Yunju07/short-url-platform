package com.yunju.stats_service.domain.stats.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UrlDeviceClicksId implements Serializable {

    private String shortKey;
    private String deviceType; // MOBILE, DESKTOP 등
}

