package com.yunju.shorturl_app.global.apiPayload.exception;

import com.yunju.shorturl_app.global.apiPayload.ApiResponse;
import com.yunju.shorturl_app.global.apiPayload.code.BaseCode;
import com.yunju.shorturl_app.global.apiPayload.code.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    // 기본 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(ErrorStatus.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.onFailure(ErrorStatus.BAD_REQUEST));
    }

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomApiException(CustomApiException ex) {
        log.error(ex.getMessage(), ex);
        BaseCode error = ex.getErrorCode();
        return ResponseEntity
                .status(error.getStatus())
                .body(ApiResponse.onFailure(error));
    }
}
