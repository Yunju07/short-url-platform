package com.yunju.common.util;

import com.yunju.common.enums.DeviceType;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

public class DeviceTypeParser {

    // YAUAA 객체는 초기화 비용이 크기 때문에 싱글톤 생성
    private static final UserAgentAnalyzer ANALYZER = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withField("DeviceClass")
            .build();

    public static DeviceType parse(String userAgent) {

        if (userAgent == null || userAgent.isBlank()) {
            return DeviceType.UNKNOWN;
        }

        UserAgent ua = ANALYZER.parse(userAgent);
        String deviceClass = ua.getValue("DeviceClass");

        if (deviceClass == null) {
            return DeviceType.UNKNOWN;
        }

        return switch (deviceClass.toUpperCase()) {
            case "MOBILE" -> DeviceType.MOBILE;
            case "TABLET" -> DeviceType.TABLET;
            case "DESKTOP" -> DeviceType.DESKTOP;
            case "ROBOT", "BOT", "SPIDER" -> DeviceType.BOT;
            default -> DeviceType.UNKNOWN;
        };
    }
}
