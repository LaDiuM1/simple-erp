package io.github.ladium1.erp.dashboard.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardKpiResponse;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardSummaryResponse;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescustomer.api.SalesCustomerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * 대시보드 종합 응답 — 각 도메인 Api 만 호출하고 자체 엔티티는 보유하지 않음.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RECENT_LIMIT = 5;

    private final CustomerApi customerApi;
    private final SalesContactApi salesContactApi;
    private final EmployeeApi employeeApi;
    private final SalesCustomerApi salesCustomerApi;

    public DashboardSummaryResponse getSummary() {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();

        DashboardKpiResponse kpi = DashboardKpiResponse.builder()
                .totalCustomers(customerApi.count())
                .totalSalesContacts(salesContactApi.count())
                .activeEmployees(employeeApi.countActive())
                .monthlySalesActivities(salesCustomerApi.countActivitiesSince(startOfMonth))
                .build();

        return DashboardSummaryResponse.builder()
                .kpi(kpi)
                .recentCustomers(customerApi.findRecent(RECENT_LIMIT))
                .recentActivities(salesCustomerApi.findRecentActivities(RECENT_LIMIT))
                .build();
    }
}
