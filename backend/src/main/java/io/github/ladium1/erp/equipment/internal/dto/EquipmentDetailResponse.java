package io.github.ladium1.erp.equipment.internal.dto;

import io.github.ladium1.erp.equipment.internal.entity.OutputUnit;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record EquipmentDetailResponse(
        Long id,
        Long customerId,
        String customerName,
        Long contractId,
        String contractNo,
        Long supplierId,
        String supplierName,
        Long productId,
        String productModelName,
        String categoryName,
        BigDecimal outputValue,
        OutputUnit outputUnit,
        String serialNo,
        String installAddress,
        LocalDate installedDate,
        LocalDate confirmedDate,
        LocalDate warrantyStartDate,
        Integer oscillatorWarrantyMonths,
        Integer generalWarrantyMonths,
        LocalDate oscillatorWarrantyEndDate,
        LocalDate generalWarrantyEndDate,
        boolean warrantyInsurance,
        String note
) {
}
