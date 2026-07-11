package io.github.ladium1.erp.contract.api.dto;

import lombok.Builder;

/**
 * 수금 vs 미수 현황 — 대시보드 위젯용. 계약취소 건 제외 전체 누적.
 */
@Builder
public record ContractOutstandingSummary(
        /** Σ최종 계약금액 (원, VAT 별도) */
        long totalFinalAmount,
        /** Σ입금액 */
        long totalPaidAmount,
        /** Σ최종 계약금액 − Σ입금액 */
        long totalOutstandingAmount
) {
}
