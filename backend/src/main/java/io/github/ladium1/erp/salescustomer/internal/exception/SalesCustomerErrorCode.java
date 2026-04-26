package io.github.ladium1.erp.salescustomer.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SalesCustomerErrorCode implements ErrorCode {

    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 영업 활동입니다."),
    ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 영업 담당 배정입니다."),
    ASSIGNMENT_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "이미 종료된 배정입니다."),
    INVALID_END_DATE(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
