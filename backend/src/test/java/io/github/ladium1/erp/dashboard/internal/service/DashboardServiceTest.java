package io.github.ladium1.erp.dashboard.internal.service;

import io.github.ladium1.erp.afterservice.api.AfterServiceApi;
import io.github.ladium1.erp.contract.api.ContractApi;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.equipment.api.EquipmentApi;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescustomer.api.SalesCustomerApi;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks private DashboardService dashboardService;

    @Mock private CustomerApi customerApi;
    @Mock private SalesContactApi salesContactApi;
    @Mock private EmployeeApi employeeApi;
    @Mock private SalesCustomerApi salesCustomerApi;
    @Mock private ContractApi contractApi;
    @Mock private AfterServiceApi afterServiceApi;
    @Mock private EquipmentApi equipmentApi;
    @Mock private ProductApi productApi;
    @Mock private MenuPermissionEvaluator menuPermissionEvaluator;
    @Mock private Authentication authentication;

    @Test
    @DisplayName("summary는 고객·영업 KPI와 최근 목록을 현재 조회자 가시성 API로 조회")
    void summary_uses_current_viewer_visibility_boundaries() {
        given(menuPermissionEvaluator.canRead(authentication, "CUSTOMERS")).willReturn(true);
        given(menuPermissionEvaluator.canRead(authentication, "SALES_CONTACTS")).willReturn(true);
        given(menuPermissionEvaluator.canRead(authentication, "EMPLOYEES")).willReturn(true);
        given(menuPermissionEvaluator.canRead(authentication, "SALES_CUSTOMERS")).willReturn(true);
        given(customerApi.countVisibleToCurrentViewer()).willReturn(2L);
        given(customerApi.findRecentVisibleToCurrentViewer(5)).willReturn(List.of());
        given(salesContactApi.count()).willReturn(4L);
        given(employeeApi.countCurrentlyEmployed()).willReturn(3L);
        given(salesCustomerApi.countVisibleActivitiesSince(any(LocalDateTime.class))).willReturn(1L);
        given(salesCustomerApi.findRecentVisibleActivities(5)).willReturn(List.of());

        var summary = dashboardService.getSummary(authentication);

        assertThat(summary.kpi().totalCustomers()).isEqualTo(2L);
        assertThat(summary.kpi().monthlySalesActivities()).isEqualTo(1L);
        assertThat(summary.recentCustomers()).isEmpty();
        assertThat(summary.recentActivities()).isEmpty();
        verify(customerApi).countVisibleToCurrentViewer();
        verify(customerApi).findRecentVisibleToCurrentViewer(5);
        verify(salesCustomerApi).countVisibleActivitiesSince(any(LocalDateTime.class));
        verify(salesCustomerApi).findRecentVisibleActivities(5);
    }

    @Test
    @DisplayName("summary는 권한 없는 메뉴의 KPI와 최근 업무 정보를 조회하지 않는다")
    void summary_omits_unauthorized_menu_data() {
        given(menuPermissionEvaluator.canRead(authentication, "CUSTOMERS")).willReturn(false);
        given(menuPermissionEvaluator.canRead(authentication, "SALES_CONTACTS")).willReturn(false);
        given(menuPermissionEvaluator.canRead(authentication, "EMPLOYEES")).willReturn(false);
        given(menuPermissionEvaluator.canRead(authentication, "SALES_CUSTOMERS")).willReturn(false);

        var summary = dashboardService.getSummary(authentication);

        assertThat(summary.kpi().totalCustomers()).isNull();
        assertThat(summary.kpi().totalSalesContacts()).isNull();
        assertThat(summary.kpi().activeEmployees()).isNull();
        assertThat(summary.kpi().monthlySalesActivities()).isNull();
        assertThat(summary.recentCustomers()).isNull();
        assertThat(summary.recentActivities()).isNull();
        verify(customerApi, never()).countVisibleToCurrentViewer();
        verify(customerApi, never()).findRecentVisibleToCurrentViewer(anyInt());
        verify(salesContactApi, never()).count();
        verify(employeeApi, never()).countCurrentlyEmployed();
        verify(salesCustomerApi, never()).countVisibleActivitiesSince(any());
        verify(salesCustomerApi, never()).findRecentVisibleActivities(anyInt());
    }
}
