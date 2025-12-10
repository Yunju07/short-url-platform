package com.yunju.redirect_service.domain.redirect.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "url_read_model")
public class UrlDocument {

    @Id
    private String id; // (=shortKey)

    private String originalUrl;

    // epoch seconds (ex: System.currentTimeMillis() / 1000)
    private Long expiredAtEpochSec;

    public UrlDocument(String shortKey, String originalUrl, Long expiredAtEpochSec) {
        this.id = shortKey;
        this.originalUrl = originalUrl;
        this.expiredAtEpochSec = expiredAtEpochSec;
    }

}
