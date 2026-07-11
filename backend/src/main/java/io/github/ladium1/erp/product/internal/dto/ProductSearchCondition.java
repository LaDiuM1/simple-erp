package io.github.ladium1.erp.product.internal.dto;

import io.github.ladium1.erp.product.internal.entity.ProductCategory;

public record ProductSearchCondition(
        String modelNameKeyword,

        ProductCategory category,

        Long supplierId,

        Boolean active
) {
}
