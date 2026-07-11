package io.github.ladium1.erp.supplier.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SupplierErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공급사입니다."),
    DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 등록된 공급사명입니다.");

    private final HttpStatus status;
    private final String message;

}
