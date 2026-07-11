package io.github.ladium1.erp.supplier.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupplierCreateRequest(
        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 100)
        String nameKo,

        @Size(max = 50)
        String country,

        String note,

        @NotNull
        Boolean active
) {
}
