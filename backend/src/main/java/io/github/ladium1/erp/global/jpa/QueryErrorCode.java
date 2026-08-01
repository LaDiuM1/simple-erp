package io.github.ladium1.erp.global.jpa;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QueryErrorCode implements ErrorCode {
    INVALID_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 기준입니다.");

    private final HttpStatus status;
    private final String message;
}
