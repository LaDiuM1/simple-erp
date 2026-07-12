package io.github.ladium1.erp.dashboard.internal.dto;

import lombok.Builder;

import java.time.LocalDate;

/**
 * 보증 만료 임박 설비 위젯 행 — equipment 원본에 고객사 / 모델명을 enrich 한 표시용.
 */
@Builder
public record ExpiringWarrantyItem(
        Long equipmentId,
        String customerName,
        String productModelName,
        String serialNo,
        LocalDate oscillatorWarrantyEndDate,
        LocalDate generalWarrantyEndDate
) {
}
