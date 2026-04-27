package io.github.ladium1.erp.dashboard.internal.dto;

import lombok.Builder;

@Builder
public record DashboardKpiResponse(
        long totalCustomers,
        long totalSalesContacts,
        long activeEmployees,
        long monthlySalesActivities
) {
}
