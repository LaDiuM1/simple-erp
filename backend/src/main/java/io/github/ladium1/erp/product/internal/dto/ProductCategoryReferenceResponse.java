package io.github.ladium1.erp.product.internal.dto;

import lombok.Builder;

/** 제품 참조 검색 필터에서 사용하는 카테고리 정보. */
@Builder
public record ProductCategoryReferenceResponse(
        Long id,
        String name
) {
}
