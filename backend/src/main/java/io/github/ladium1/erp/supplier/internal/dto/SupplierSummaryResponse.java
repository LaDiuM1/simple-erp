package io.github.ladium1.erp.supplier.internal.dto;

import lombok.Builder;

@Builder
public record SupplierSummaryResponse(
        Long id,
        String name,
        String nameKo,
        String country,
        boolean active
) {
}
