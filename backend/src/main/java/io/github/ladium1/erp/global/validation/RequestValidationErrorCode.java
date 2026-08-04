package io.github.ladium1.erp.global.validation;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RequestValidationErrorCode implements ErrorCode {

    INVALID_MUTATION_BATCH(HttpStatus.BAD_REQUEST, "일괄 변경은 한 번에 최대 20개의 유효한 항목만 처리할 수 있습니다."),
    AMOUNT_TOTAL_EXCEEDED(HttpStatus.BAD_REQUEST, "금액 합계가 허용 범위를 초과했습니다.");

    private final HttpStatus status;
    private final String message;
}
