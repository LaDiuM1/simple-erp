package io.github.ladium1.erp.product.internal.dto;

import lombok.Builder;

@Builder
public record ProductSummaryResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String modelName,
        Long supplierId,
        String supplierName,
        boolean active
) {
}
