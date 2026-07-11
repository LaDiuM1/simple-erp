package io.github.ladium1.erp.product.internal.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 카테고리 노출 순서 일괄 재배치 — 화면에 보이는 순서 그대로 전체 ID 를 전달한다.
 */
public record ProductCategoryReorderRequest(
        @NotEmpty
        List<Long> orderedIds
) {
}
