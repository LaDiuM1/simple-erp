package io.github.ladium1.erp.product.internal.dto;

import lombok.Builder;

/** 계약과 설비에서 제품을 선택할 때 필요한 최소 정보. */
@Builder
public record ProductReferenceResponse(
        Long id,
        String modelName,
        String categoryName,
        String supplierName,
        boolean active
) {
}
