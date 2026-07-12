package io.github.ladium1.erp.contract.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContractErrorCode implements ErrorCode {

    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 계약입니다."),
    DUPLICATE_CONTRACT_NO(HttpStatus.CONFLICT, "이미 등록된 계약 번호입니다."),
    CONTRACT_NO_REQUIRED(HttpStatus.BAD_REQUEST, "계약 번호를 입력해주세요."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 대금 회차입니다."),
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 계약 메모입니다."),
    AUTHOR_NOT_RESOLVED(HttpStatus.UNAUTHORIZED, "작성자 정보를 확인할 수 없습니다."),
    CUSTOMER_IN_USE(HttpStatus.BAD_REQUEST, "계약이 참조하는 고객사는 삭제할 수 없습니다."),
    PRODUCT_IN_USE(HttpStatus.BAD_REQUEST, "계약이 참조하는 제품 모델은 삭제할 수 없습니다."),
    SUPPLIER_IN_USE(HttpStatus.BAD_REQUEST, "계약이 참조하는 공급사는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
