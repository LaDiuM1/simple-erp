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
    INVALID_EMPLOYEE(HttpStatus.BAD_REQUEST, "계약 담당자는 재직 중인 직원만 지정할 수 있습니다."),
    EMPLOYEE_OUT_OF_SCOPE(HttpStatus.FORBIDDEN, "데이터 조회 범위 밖의 직원을 계약 담당자로 지정할 수 없습니다."),
    INACTIVE_PRODUCT(HttpStatus.BAD_REQUEST, "사용 중인 제품 모델만 새 계약에 연결할 수 있습니다."),
    INVALID_DATE_FLOW(HttpStatus.BAD_REQUEST, "계약 상태와 일정 날짜의 흐름이 올바르지 않습니다."),
    INSTALLED_CONTRACT_SNAPSHOT_IMMUTABLE(
            HttpStatus.BAD_REQUEST, "설비가 생성된 계약의 고객사·제품·출력·설치일과 완료 상태는 변경할 수 없습니다."),
    INSTALLED_CONTRACT_DELETE_FORBIDDEN(
            HttpStatus.BAD_REQUEST, "설비 생성 대상이 된 계약은 삭제할 수 없습니다."),
    CUSTOMER_IN_USE(HttpStatus.BAD_REQUEST, "계약이 참조하는 고객사는 삭제할 수 없습니다."),
    PRODUCT_IN_USE(HttpStatus.BAD_REQUEST, "계약이 참조하는 제품 모델은 삭제할 수 없습니다."),
    SUPPLIER_IN_USE(HttpStatus.BAD_REQUEST, "계약이 참조하는 공급사는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
