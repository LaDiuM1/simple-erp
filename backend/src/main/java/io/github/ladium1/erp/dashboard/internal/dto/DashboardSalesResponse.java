package io.github.ladium1.erp.dashboard.internal.dto;

import io.github.ladium1.erp.contract.api.dto.ContractOutstandingSummary;
import io.github.ladium1.erp.contract.api.dto.MonthlyContractStat;
import lombok.Builder;

import java.util.List;

/**
 * 영업 위젯 — CONTRACTS read 권한자만. 데이터 스코프는 contract 모듈이 적용.
 */
@Builder
public record DashboardSalesResponse(
        List<MonthlyContractStat> monthlyStats,
        ContractOutstandingSummary outstanding
) {
}
