package io.github.ladium1.erp.dashboard.internal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.ladium1.erp.customer.api.dto.RecentCustomerInfo;
import io.github.ladium1.erp.salescustomer.api.dto.RecentSalesActivityInfo;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardSummaryResponse(
        DashboardKpiResponse kpi,
        List<RecentCustomerInfo> recentCustomers,
        List<RecentSalesActivityInfo> recentActivities
) {
}
