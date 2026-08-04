package io.github.ladium1.erp.dashboard.internal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardKpiResponse(
        Long totalCustomers,
        Long totalSalesContacts,
        Long activeEmployees,
        Long monthlySalesActivities
) {
}
