package io.github.ladium1.erp.supplier.internal.dto;

import lombok.Builder;

@Builder
public record SupplierDetailResponse(
        Long id,
        String name,
        String nameKo,
        String country,
        String note,
        boolean active
) {
}
