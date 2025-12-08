package com.yunju.stats_service.global.apiPayload.code.status;

import com.yunju.stats_service.global.apiPayload.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus implements BaseCode {

    BAD_REQUEST(400, "BAD_REQUEST", "요청 형식 또는 값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_ERROR", "요청을 처리하는 도중 서버에서 문제가 발생했습니다."),
    STATS_NOT_FOUND(404, "STATS_NOT_FOUND", "요청한 단축 URL의 통계가 존재하지 않습니다.");

    private final int status;
    private final String code;
    private final String message;
}