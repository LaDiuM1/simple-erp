package io.github.ladium1.erp.dashboard.internal.dto;

import io.github.ladium1.erp.customer.api.dto.RecentCustomerInfo;
import io.github.ladium1.erp.salescustomer.api.dto.FollowUpCustomerInfo;
import io.github.ladium1.erp.salescustomer.api.dto.RecentSalesActivityInfo;
import io.github.ladium1.erp.salescustomer.api.dto.WeeklyActivityCountInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record DashboardSummaryResponse(
        DashboardKpiResponse kpi,
        List<RecentCustomerInfo> recentCustomers,
        List<RecentSalesActivityInfo> recentActivities,
        List<WeeklyActivityCountInfo> weeklyActivityTrend,
        List<FollowUpCustomerInfo> followUps,
        long newCustomersThisWeek,
        long newSalesContactsThisWeek,
        long newSalesContactsThisMonth,
        long uncontactedCustomersThisMonth
) {
}
