package io.github.ladium1.erp.product.internal.dto;

import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 카테고리 노출 순서 일괄 재배치 — 화면에 보이는 순서 그대로 전체 ID 를 전달한다.
 */
public record ProductCategoryReorderRequest(
        @NotEmpty @Size(max = RequestCollectionPolicy.MAX_FULL_REORDER_SIZE)
        List<@NotNull Long> orderedIds
) {
}
