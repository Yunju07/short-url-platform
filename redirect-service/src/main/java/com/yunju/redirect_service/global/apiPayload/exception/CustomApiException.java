package com.yunju.redirect_service.global.apiPayload.exception;

import com.yunju.redirect_service.global.apiPayload.code.BaseCode;
import lombok.Getter;

@Getter
public class CustomApiException extends RuntimeException {
    private final BaseCode errorCode;

    public CustomApiException(BaseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
