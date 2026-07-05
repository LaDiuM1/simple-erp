package io.github.ladium1.erp.dashboard.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardSummaryResponse;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescustomer.api.SalesCustomerApi;
import io.github.ladium1.erp.salescustomer.api.dto.FollowUpCustomerInfo;
import io.github.ladium1.erp.salescustomer.api.dto.WeeklyActivityCountInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock private CustomerApi customerApi;
    @Mock private SalesContactApi salesContactApi;
    @Mock private EmployeeApi employeeApi;
    @Mock private SalesCustomerApi salesCustomerApi;

    @Test
    @DisplayName("summary 성공 — KPI + 추이 + 팔로업 + 기간 증감 통합 반환")
    void get_summary_success() {
        // given
        given(customerApi.count()).willReturn(312L);
        given(salesContactApi.count()).willReturn(5214L);
        given(employeeApi.countActive()).willReturn(28L);
        given(salesCustomerApi.countActivitiesSince(any())).willReturn(47L);
        given(customerApi.findRecent(anyInt())).willReturn(List.of());
        given(salesCustomerApi.findRecentActivities(anyInt())).willReturn(List.of());
        given(salesCustomerApi.countActivitiesByWeek(8)).willReturn(List.of(
                WeeklyActivityCountInfo.builder().weekStart(LocalDate.of(2026, 6, 29)).count(47L).build()));
        given(customerApi.resolveVisibleCustomerIds()).willReturn(Optional.empty());
        given(salesCustomerApi.findFollowUpTargets(anyInt(), anyInt(), eq(null))).willReturn(List.of(
                FollowUpCustomerInfo.builder().customerId(2L).customerName("대양테크").build()));
        given(customerApi.countCreatedSince(any())).willReturn(6L);
        given(salesContactApi.countCreatedSince(any())).willReturn(31L);
        given(salesCustomerApi.countDistinctCustomersWithActivitySince(any())).willReturn(289L);

        // when
        DashboardSummaryResponse summary = dashboardService.getSummary();

        // then
        assertThat(summary.kpi().totalCustomers()).isEqualTo(312L);
        assertThat(summary.weeklyActivityTrend()).hasSize(1);
        assertThat(summary.followUps()).hasSize(1);
        assertThat(summary.newCustomersThisWeek()).isEqualTo(6L);
        assertThat(summary.newSalesContactsThisWeek()).isEqualTo(31L);
        assertThat(summary.uncontactedCustomersThisMonth()).isEqualTo(23L);
    }

    @Test
    @DisplayName("팔로업에 고객 데이터 스코프 전달 — 가시 집합이 있으면 그대로 위임")
    void get_summary_forwards_visible_scope() {
        // given
        given(customerApi.count()).willReturn(10L);
        given(salesContactApi.count()).willReturn(0L);
        given(employeeApi.countActive()).willReturn(0L);
        given(salesCustomerApi.countActivitiesSince(any())).willReturn(0L);
        given(customerApi.findRecent(anyInt())).willReturn(List.of());
        given(salesCustomerApi.findRecentActivities(anyInt())).willReturn(List.of());
        given(salesCustomerApi.countActivitiesByWeek(anyInt())).willReturn(List.of());
        given(customerApi.resolveVisibleCustomerIds()).willReturn(Optional.of(Set.of(1L, 2L)));
        given(salesCustomerApi.findFollowUpTargets(anyInt(), anyInt(), eq(Set.of(1L, 2L)))).willReturn(List.of());
        given(customerApi.countCreatedSince(any())).willReturn(0L);
        given(salesContactApi.countCreatedSince(any())).willReturn(0L);
        given(salesCustomerApi.countDistinctCustomersWithActivitySince(any())).willReturn(0L);

        // when
        dashboardService.getSummary();

        // then
        verify(salesCustomerApi).findFollowUpTargets(anyInt(), anyInt(), eq(Set.of(1L, 2L)));
    }
}
