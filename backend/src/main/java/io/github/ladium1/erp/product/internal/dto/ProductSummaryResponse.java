package io.github.ladium1.erp.product.internal.dto;

import io.github.ladium1.erp.product.internal.entity.ProductCategory;
import lombok.Builder;

@Builder
public record ProductSummaryResponse(
        Long id,
        ProductCategory category,
        String modelName,
        Long supplierId,
        String supplierName,
        boolean active
) {
}
