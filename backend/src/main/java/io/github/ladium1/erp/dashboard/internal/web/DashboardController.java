package io.github.ladium1.erp.dashboard.internal.web;

import io.github.ladium1.erp.dashboard.internal.dto.DashboardSalesResponse;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardServiceResponse;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardSummaryResponse;
import io.github.ladium1.erp.dashboard.internal.dto.ExpiringWarrantyItem;
import io.github.ladium1.erp.dashboard.internal.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 모든 인증 사용자가 접근하는 홈 — 종합 (summary) 은 메뉴 권한 체크 없음 (전역 authenticated() 만 적용).
 * 계약 금액 / AS 원가처럼 민감한 위젯은 해당 메뉴 read 권한으로 분리 노출 — FE 가 권한별로 섹션을 감춘다.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final String CAN_READ_CONTRACTS =
            "@menuPermissionEvaluator.canRead(authentication, 'CONTRACTS')";
    private static final String CAN_READ_AFTER_SERVICES =
            "@menuPermissionEvaluator.canRead(authentication, 'AFTER_SERVICES')";
    private static final String CAN_READ_EQUIPMENTS =
            "@menuPermissionEvaluator.canRead(authentication, 'EQUIPMENTS')";

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/sales")
    @PreAuthorize(CAN_READ_CONTRACTS)
    public DashboardSalesResponse getSales() {
        return dashboardService.getSales();
    }

    @GetMapping("/service")
    @PreAuthorize(CAN_READ_AFTER_SERVICES)
    public DashboardServiceResponse getService() {
        return dashboardService.getService();
    }

    @GetMapping("/warranty")
    @PreAuthorize(CAN_READ_EQUIPMENTS)
    public List<ExpiringWarrantyItem> getExpiringWarranties() {
        return dashboardService.getExpiringWarranties();
    }
}
