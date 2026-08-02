package io.github.ladium1.erp.equipment.internal.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record EquipmentReferenceResponse(
        Long id,
        Long customerId,
        String productModelName,
        String serialNo,
        String installAddress,
        LocalDate installedDate,
        LocalDate oscillatorWarrantyEndDate,
        LocalDate generalWarrantyEndDate
) {
}
