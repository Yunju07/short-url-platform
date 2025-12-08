package com.yunju.stats_service.domain.stats.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UrlReferrerClicksId implements Serializable {

    private String shortKey;
    private String referrer;
}
