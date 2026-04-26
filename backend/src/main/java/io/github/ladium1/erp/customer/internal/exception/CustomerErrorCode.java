package io.github.ladium1.erp.customer.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomerErrorCode implements ErrorCode {

    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 고객사입니다."),
    DUPLICATE_CODE(HttpStatus.CONFLICT, "이미 사용 중인 고객사 코드입니다."),
    DUPLICATE_BIZ_REG_NO(HttpStatus.CONFLICT, "이미 등록된 사업자등록번호입니다."),
    CODE_REQUIRED(HttpStatus.BAD_REQUEST, "고객사 코드를 입력해주세요.");

    private final HttpStatus status;
    private final String message;

}
