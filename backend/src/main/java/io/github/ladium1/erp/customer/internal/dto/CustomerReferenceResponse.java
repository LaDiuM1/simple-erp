package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import lombok.Builder;

/**
 * 업무 화면에서 고객사를 식별하는 데 필요한 최소 정보.
 * 고객사 관리 전용 상세 정보와 참조 검색 응답을 분리한다.
 */
@Builder
public record CustomerReferenceResponse(
        Long id,
        String code,
        String name,
        String representative,
        String phone,
        CustomerType type,
        CustomerStatus status
) {
}
