package io.github.ladium1.erp.expense.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExpenseErrorCode implements ErrorCode {

    CLAIM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경비 청구입니다."),
    EMPTY_ITEMS(HttpStatus.BAD_REQUEST, "경비 항목을 1건 이상 입력해주세요."),
    TOTAL_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "경비 청구 총액이 허용 범위를 초과했습니다.");

    private final HttpStatus status;
    private final String message;

}
