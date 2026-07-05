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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

/**
 * 대시보드 종합 응답 — 각 도메인 Api 만 호출하고 자체 엔티티는 보유하지 않음.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RECENT_LIMIT = 5;
    private static final int TREND_WEEKS = 8;
    private static final int FOLLOW_UP_STALE_DAYS = 7;
    private static final int FOLLOW_UP_LIMIT = 5;

    private final CustomerApi customerApi;
    private final SalesContactApi salesContactApi;
    private final EmployeeApi employeeApi;
    private final SalesCustomerApi salesCustomerApi;

    public DashboardSummaryResponse getSummary() {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        DashboardKpiResponse kpi = DashboardKpiResponse.builder()
                .totalCustomers(customerApi.count())
                .totalSalesContacts(salesContactApi.count())
                .activeEmployees(employeeApi.countActive())
                .monthlySalesActivities(salesCustomerApi.countActivitiesSince(startOfMonth))
                .build();

        // 팔로업은 고객 데이터 스코프 (내 담당 / 부서) 를 그대로 적용 — 가시 집합이 빈 Set 이면 결과도 빈 목록
        Set<Long> visibleCustomerIds = customerApi.resolveVisibleCustomerIds().orElse(null);

        long uncontacted = kpi.totalCustomers() - salesCustomerApi.countDistinctCustomersWithActivitySince(startOfMonth);

        return DashboardSummaryResponse.builder()
                .kpi(kpi)
                .recentCustomers(customerApi.findRecent(RECENT_LIMIT))
                .recentActivities(salesCustomerApi.findRecentActivities(RECENT_LIMIT))
                .weeklyActivityTrend(salesCustomerApi.countActivitiesByWeek(TREND_WEEKS))
                .followUps(salesCustomerApi.findFollowUpTargets(FOLLOW_UP_STALE_DAYS, FOLLOW_UP_LIMIT, visibleCustomerIds))
                .newCustomersThisWeek(customerApi.countCreatedSince(startOfWeek))
                .newSalesContactsThisWeek(salesContactApi.countCreatedSince(startOfWeek))
                .newSalesContactsThisMonth(salesContactApi.countCreatedSince(startOfMonth))
                .uncontactedCustomersThisMonth(Math.max(0, uncontacted))
                .build();
    }
}
