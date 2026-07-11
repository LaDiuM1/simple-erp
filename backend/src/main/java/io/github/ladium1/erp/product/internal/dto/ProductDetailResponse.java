package io.github.ladium1.erp.product.internal.dto;

import lombok.Builder;

@Builder
public record ProductDetailResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String modelName,
        Long supplierId,
        String supplierName,
        String note,
        boolean active
) {
}
