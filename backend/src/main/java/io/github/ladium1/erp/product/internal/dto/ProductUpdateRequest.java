package io.github.ladium1.erp.product.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotNull
        Long categoryId,

        @NotBlank @Size(max = 100)
        String modelName,

        @NotNull
        Long supplierId,

        String note,

        @NotNull
        Boolean active
) {
}
