package io.github.ladium1.erp.dashboard.internal.service;

import io.github.ladium1.erp.afterservice.api.AfterServiceApi;
import io.github.ladium1.erp.contract.api.ContractApi;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardKpiResponse;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardSalesResponse;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardServiceResponse;
import io.github.ladium1.erp.dashboard.internal.dto.DashboardSummaryResponse;
import io.github.ladium1.erp.dashboard.internal.dto.ExpiringWarrantyItem;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.equipment.api.EquipmentApi;
import io.github.ladium1.erp.equipment.api.dto.ExpiringWarrantyInfo;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.product.api.dto.ProductInfo;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescustomer.api.SalesCustomerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * 대시보드 종합 응답 — 각 도메인 Api 만 호출하고 자체 엔티티는 보유하지 않음.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RECENT_LIMIT = 5;
    /** 영업 / AS 위젯 집계 구간 — 최근 6개월. */
    private static final int STAT_MONTHS = 6;
    /** 보증 만료 임박 기준 — 90일 내 만료 / 최대 5건. */
    private static final int WARRANTY_EXPIRING_DAYS = 90;
    private static final int WARRANTY_LIMIT = 5;

    private final CustomerApi customerApi;
    private final SalesContactApi salesContactApi;
    private final EmployeeApi employeeApi;
    private final SalesCustomerApi salesCustomerApi;
    private final ContractApi contractApi;
    private final AfterServiceApi afterServiceApi;
    private final EquipmentApi equipmentApi;
    private final ProductApi productApi;

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

    /**
     * 영업 위젯 — 월별 계약 실적 + 수금 vs 미수. 행 가시성 (데이터 스코프) 은 contract 모듈이 적용.
     */
    public DashboardSalesResponse getSales() {
        return DashboardSalesResponse.builder()
                .monthlyStats(contractApi.monthlyStats(STAT_MONTHS))
                .outstanding(contractApi.outstandingSummary())
                .build();
    }

    /**
     * AS 위젯 — 유형별 건수 / 원가 + 엔지니어별 원가 (최근 {@value STAT_MONTHS}개월).
     */
    public DashboardServiceResponse getService() {
        return DashboardServiceResponse.builder()
                .typeStats(afterServiceApi.typeStats(STAT_MONTHS))
                .engineerStats(afterServiceApi.engineerExpenseStats(STAT_MONTHS))
                .build();
    }

    /**
     * 보증 만료 임박 설비 위젯 — 고객사 / 모델명을 enrich 해 표시용 행으로 조립.
     */
    public List<ExpiringWarrantyItem> getExpiringWarranties() {
        List<ExpiringWarrantyInfo> equipments =
                equipmentApi.findExpiringWarranties(WARRANTY_EXPIRING_DAYS, WARRANTY_LIMIT);
        if (equipments.isEmpty()) {
            return List.of();
        }
        Map<Long, String> customerNames = customerApi.findByIds(
                        equipments.stream().map(ExpiringWarrantyInfo::customerId).distinct().toList()).stream()
                .collect(toMap(CustomerInfo::id, CustomerInfo::name));
        Map<Long, String> productNames = productApi.findByIds(
                        equipments.stream().map(ExpiringWarrantyInfo::productId).distinct().toList()).stream()
                .collect(toMap(ProductInfo::id, ProductInfo::modelName));

        return equipments.stream()
                .map(e -> ExpiringWarrantyItem.builder()
                        .equipmentId(e.id())
                        .customerName(customerNames.get(e.customerId()))
                        .productModelName(productNames.get(e.productId()))
                        .serialNo(e.serialNo())
                        .oscillatorWarrantyEndDate(e.oscillatorWarrantyEndDate())
                        .generalWarrantyEndDate(e.generalWarrantyEndDate())
                        .build())
                .toList();
    }
}
