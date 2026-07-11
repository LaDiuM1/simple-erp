package io.github.ladium1.erp.equipment.api.dto;

import lombok.Builder;

import java.time.LocalDate;

/**
 * 외부 모듈 노출용 설비 요약 — AS 접수의 참조 검증 / 보증 판정 / 표시 enrichment 용.
 */
@Builder
public record EquipmentInfo(
        Long id,
        Long customerId,
        Long productId,
        String serialNo,
        LocalDate oscillatorWarrantyEndDate,
        LocalDate generalWarrantyEndDate
) {
}
