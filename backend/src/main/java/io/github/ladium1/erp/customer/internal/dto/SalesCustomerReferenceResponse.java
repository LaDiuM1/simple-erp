package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import lombok.Builder;

/** 영업 고객 목록과 상세 머리글에 필요한 고객사 정보. */
@Builder
public record SalesCustomerReferenceResponse(
        Long id,
        String code,
        String name,
        String representative,
        String phone,
        String zipCode,
        String roadAddress,
        String detailAddress,
        CustomerType type,
        CustomerStatus status
) {
}
