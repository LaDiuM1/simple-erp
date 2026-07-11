package io.github.ladium1.erp.product.api.dto;

import lombok.Builder;

@Builder
public record ProductInfo(
        Long id,
        Long categoryId,
        String categoryName,
        String modelName,
        Long supplierId,
        boolean active
) {
}
