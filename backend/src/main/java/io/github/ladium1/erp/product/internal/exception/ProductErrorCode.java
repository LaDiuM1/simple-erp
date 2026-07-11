package io.github.ladium1.erp.product.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 제품 모델입니다."),
    DUPLICATE_MODEL_NAME(HttpStatus.CONFLICT, "해당 공급사에 이미 등록된 모델명입니다."),
    SUPPLIER_IN_USE(HttpStatus.BAD_REQUEST, "제품 모델이 참조하는 공급사는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
