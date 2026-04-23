package com.yunju.url_service.infra.shortkey;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shortkey")
public class ShortKeyProperties {
    private int length = 6;
    private String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private int maxUniqueAttempts = 5;
}
