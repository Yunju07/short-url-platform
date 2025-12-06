package com.yunju.url_service.global.apiPayload.code.status;

import com.yunju.url_service.global.apiPayload.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {

    OK(200, "SUCCESS", "요청이 성공적으로 처리되었습니다."),
    CREATED(201, "CREATED", "리소스가 성공적으로 생성되었습니다.");

    private final int status;
    private final String code;
    private final String message;
}