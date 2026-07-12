package io.github.ladium1.erp.afterservice.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AfterServiceErrorCode implements ErrorCode {

    AFTER_SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 AS 건입니다."),
    DUPLICATE_RECEIPT_NO(HttpStatus.CONFLICT, "이미 등록된 접수번호입니다."),
    RECEIPT_NO_REQUIRED(HttpStatus.BAD_REQUEST, "접수번호를 입력해주세요."),
    VISIT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 방문 일지입니다."),
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경비 항목입니다."),
    ENGINEER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 엔지니어입니다."),
    ENGINEER_IN_USE(HttpStatus.BAD_REQUEST, "AS 기록이 참조하는 엔지니어는 삭제할 수 없습니다."),
    CUSTOMER_IN_USE(HttpStatus.BAD_REQUEST, "AS 건이 참조하는 고객사는 삭제할 수 없습니다."),
    EQUIPMENT_IN_USE(HttpStatus.BAD_REQUEST, "AS 건이 참조하는 설비는 삭제할 수 없습니다."),
    EQUIPMENT_CUSTOMER_MISMATCH(HttpStatus.BAD_REQUEST, "선택한 설비가 해당 고객사의 설비가 아닙니다.");

    private final HttpStatus status;
    private final String message;

}
