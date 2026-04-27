package io.github.ladium1.erp.salescontact.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SalesContactErrorCode implements ErrorCode {

    CONTACT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 영업 명부입니다."),
    DUPLICATE_MOBILE_PHONE(HttpStatus.CONFLICT, "이미 등록된 휴대폰 번호입니다."),
    EMPLOYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 재직 이력입니다."),
    EMPLOYMENT_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "이미 종료된 재직 이력입니다."),
    INVALID_END_DATE(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다."),
    COMPANY_REQUIRED(HttpStatus.BAD_REQUEST, "고객사 또는 외부 회사명 둘 중 하나는 입력해야 합니다.");

    private final HttpStatus status;
    private final String message;
}
