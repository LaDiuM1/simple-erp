package io.github.ladium1.erp.product.internal.dto;

import lombok.Builder;

@Builder
public record ProductCategoryResponse(
        Long id,
        String name,
        int sortOrder,
        /** 이 카테고리를 참조하는 제품 모델 수 — 삭제 가능 여부 판단용 표시 */
        long productCount
) {
}
