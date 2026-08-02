package io.github.ladium1.erp.equipment.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EquipmentErrorCode implements ErrorCode {

    EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 설비입니다."),
    INACTIVE_PRODUCT(HttpStatus.BAD_REQUEST, "사용 중인 제품 모델만 새 설비에 연결할 수 있습니다."),
    CONTRACT_SNAPSHOT_IMMUTABLE(
            HttpStatus.BAD_REQUEST, "계약에서 생성된 설비의 고객사·제품·출력·설치일은 변경할 수 없습니다."),
    CONTRACT_LINKED_DELETE_FORBIDDEN(
            HttpStatus.BAD_REQUEST, "계약에서 생성된 설비는 직접 삭제할 수 없습니다."),
    CUSTOMER_IN_USE(HttpStatus.BAD_REQUEST, "설비 대장이 참조하는 고객사는 삭제할 수 없습니다."),
    PRODUCT_IN_USE(HttpStatus.BAD_REQUEST, "설비 대장이 참조하는 제품 모델은 삭제할 수 없습니다."),
    SUPPLIER_IN_USE(HttpStatus.BAD_REQUEST, "설비 대장이 참조하는 공급사는 삭제할 수 없습니다."),
    CONTRACT_IN_USE(HttpStatus.BAD_REQUEST, "설비 대장이 참조하는 계약은 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
