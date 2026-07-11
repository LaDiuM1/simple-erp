package io.github.ladium1.erp.product.internal.dto;

public record ProductSearchCondition(
        String modelNameKeyword,

        Long categoryId,

        Long supplierId,

        Boolean active
) {
}
