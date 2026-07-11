package io.github.ladium1.erp.product.internal.dto;

import io.github.ladium1.erp.product.internal.entity.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
        @NotNull
        ProductCategory category,

        @NotBlank @Size(max = 100)
        String modelName,

        @NotNull
        Long supplierId,

        String note,

        @NotNull
        Boolean active
) {
}
