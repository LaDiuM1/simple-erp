package io.github.ladium1.erp.equipment.internal.dto;

public record EquipmentSearchCondition(
        Long customerId,
        Long supplierId,
        String serialKeyword,
        String addressKeyword,
        WarrantyFilter warranty
) {
}
