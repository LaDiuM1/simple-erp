package io.github.ladium1.erp.contract.api.dto;

import lombok.Builder;

/**
 * 월별 계약 실적 — 대시보드 위젯용. 계약취소 건 제외.
 */
@Builder
public record MonthlyContractStat(
        /** "2026-01" 형식 */
        String month,
        long count,
        /** Σ최종 계약금액 (원, VAT 별도) */
        long totalAmount
) {
}
