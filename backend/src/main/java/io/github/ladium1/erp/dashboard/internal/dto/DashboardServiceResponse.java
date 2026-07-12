package io.github.ladium1.erp.dashboard.internal.dto;

import io.github.ladium1.erp.afterservice.api.dto.EngineerExpenseStat;
import io.github.ladium1.erp.afterservice.api.dto.ServiceTypeStat;
import lombok.Builder;

import java.util.List;

/**
 * AS 위젯 — AFTER_SERVICES read 권한자만.
 */
@Builder
public record DashboardServiceResponse(
        List<ServiceTypeStat> typeStats,
        List<EngineerExpenseStat> engineerStats
) {
}
