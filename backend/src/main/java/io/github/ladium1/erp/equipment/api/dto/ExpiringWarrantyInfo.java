package io.github.ladium1.erp.equipment.api.dto;

import lombok.Builder;

import java.time.LocalDate;

/**
 * 보증 만료 임박 설비 — 대시보드 위젯용. 표시 이름 (고객사 / 모델) 은 호출자가 enrich.
 */
@Builder
public record ExpiringWarrantyInfo(
        Long id,
        Long customerId,
        Long productId,
        String serialNo,
        LocalDate oscillatorWarrantyEndDate,
        LocalDate generalWarrantyEndDate
) {
}
