package io.github.ladium1.erp.equipment.internal.dto;

import io.github.ladium1.erp.equipment.internal.entity.OutputUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 수정 요청 — 계약 연결 (contractId) 은 자동 생성분의 원천 추적용이라 수정 항목에 없다.
 */
public record EquipmentUpdateRequest(
        @NotNull
        Long customerId,

        @NotNull
        Long productId,

        @PositiveOrZero
        BigDecimal outputValue,

        OutputUnit outputUnit,

        @Size(max = 100)
        String serialNo,

        @Size(max = 255)
        String installAddress,

        LocalDate installedDate,

        LocalDate confirmedDate,

        LocalDate warrantyStartDate,

        @PositiveOrZero
        Integer oscillatorWarrantyMonths,

        @PositiveOrZero
        Integer generalWarrantyMonths,

        @NotNull
        Boolean warrantyInsurance,

        String note
) {
}
