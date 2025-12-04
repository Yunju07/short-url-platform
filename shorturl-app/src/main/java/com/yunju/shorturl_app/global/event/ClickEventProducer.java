package com.yunju.shorturl_app.global.event;

public interface ClickEventProducer {

    void sendClickEvent(String shortKey, String userAgent, String referer);
}
