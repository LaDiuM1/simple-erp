package io.github.ladium1.erp.supplier.api.dto;

import lombok.Builder;

@Builder
public record SupplierInfo(
        Long id,
        String name,
        String nameKo,
        String country,
        boolean active
) {
}
