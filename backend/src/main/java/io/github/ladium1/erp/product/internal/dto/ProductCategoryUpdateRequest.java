package io.github.ladium1.erp.product.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductCategoryUpdateRequest(
        @NotBlank @Size(max = 50)
        String name
) {
}
