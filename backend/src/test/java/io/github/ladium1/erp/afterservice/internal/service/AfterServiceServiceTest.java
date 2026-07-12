package io.github.ladium1.erp.afterservice.internal.service;

import io.github.ladium1.erp.afterservice.api.dto.EngineerExpenseStat;
import io.github.ladium1.erp.afterservice.api.dto.ServiceTypeStat;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceCreateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceDetailResponse;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSummaryResponse;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceExpenseRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceVisitRequest;
import io.github.ladium1.erp.afterservice.internal.entity.AfterService;
import io.github.ladium1.erp.afterservice.internal.entity.ExpensePayerType;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceExpense;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceExpenseCategory;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceVisit;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import io.github.ladium1.erp.afterservice.internal.excel.AfterServiceExcelExporter;
import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.afterservice.internal.repository.AfterServiceRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceExpenseRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceVisitRepository;
import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.equipment.api.EquipmentApi;
import io.github.ladium1.erp.equipment.api.dto.EquipmentInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.product.api.dto.ProductInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AfterServiceServiceTest {

    @InjectMocks
    private AfterServiceService afterServiceService;

    @Mock private AfterServiceRepository afterServiceRepository;
    @Mock private ServiceVisitRepository visitRepository;
    @Mock private ServiceExpenseRepository expenseRepository;
    @Mock private EngineerService engineerService;
    @Mock private AfterServiceExcelExporter excelExporter;
    @Mock private CodeRuleApi codeRuleApi;
    @Mock private CustomerApi customerApi;
    @Mock private EquipmentApi equipmentApi;
    @Mock private ProductApi productApi;

    @Test
    @DisplayName("search 성공 — 고객사 / 설비 / 엔지니어 enrich + 경비 합계")
    void search_success() {
        // given
        AfterService as = mockAfterService(1L, 10L);
        ReflectionTestUtils.setField(as, "id", 1L);
        Pageable pageable = PageRequest.of(0, 20);
        given(afterServiceRepository.search(any(AfterServiceSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(as), pageable, 1));
        stubRefNames();
        given(expenseRepository.sumAmountByAfterServiceIds(List.of(1L)))
                .willReturn(Map.of(1L, 850_000L));

        // when
        PageResponse<AfterServiceSummaryResponse> page =
                afterServiceService.search(emptyCondition(), pageable);

        // then
        AfterServiceSummaryResponse summary = page.content().getFirst();
        assertThat(summary.customerName()).isEqualTo("대성상사");
        assertThat(summary.equipmentModelName()).isEqualTo("HLA-1530");
        assertThat(summary.assignedEngineerName()).isEqualTo("문영테크 김기사");
        assertThat(summary.expenseTotal()).isEqualTo(850_000L);
    }

    @Test
    @DisplayName("getDetail 성공 — 일지 / 경비 포함 + 경비 합계 산출")
    void get_detail_success() {
        // given
        AfterService as = mockAfterService(1L, 10L);
        ReflectionTestUtils.setField(as, "id", 1L);
        given(afterServiceRepository.findById(1L)).willReturn(Optional.of(as));
        stubRefNames();

        ServiceVisit visit = ServiceVisit.builder()
                .afterServiceId(1L).visitDate(LocalDate.of(2026, 5, 2)).engineerId(5L)
                .problem("레이저 출력 저하").resolution("보호창 교체")
                .build();
        given(visitRepository.findByAfterServiceIdOrderByVisitDateDescIdDesc(1L)).willReturn(List.of(visit));

        ServiceExpense lodging = expense(1L, ServiceExpenseCategory.LODGING, 150_000L);
        ServiceExpense wage = expense(1L, ServiceExpenseCategory.DAILY_WAGE, 700_000L);
        given(expenseRepository.findByAfterServiceIdOrderByIdAsc(1L)).willReturn(List.of(lodging, wage));
        given(engineerService.findNamesByIds(anyList())).willReturn(Map.of(5L, "문영테크 김기사"));

        // when
        AfterServiceDetailResponse detail = afterServiceService.getDetail(1L);

        // then
        assertThat(detail.expenseTotal()).isEqualTo(850_000L);
        assertThat(detail.visits()).hasSize(1);
        assertThat(detail.visits().getFirst().engineerName()).isEqualTo("문영테크 김기사");
        assertThat(detail.expenses()).hasSize(2);
    }

    @Test
    @DisplayName("getDetail 실패 — AFTER_SERVICE_NOT_FOUND")
    void get_detail_fail_not_found() {
        given(afterServiceRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> afterServiceService.getDetail(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.AFTER_SERVICE_NOT_FOUND);
    }

    @Test
    @DisplayName("create 성공 — AUTO 채번 + 설비-고객사 일치 검증 통과")
    void create_auto_mode_success() {
        // given
        AfterServiceCreateRequest request = baseCreateRequest(10L);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(equipmentApi.getById(10L)).willReturn(equipmentInfo(1L));
        given(codeRuleApi.getRule(CodeRuleTarget.AFTER_SERVICE)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.AFTER_SERVICE), any())).willReturn("AS2026-0001");
        given(afterServiceRepository.existsByReceiptNo("AS2026-0001")).willReturn(false);

        AfterService saved = mockAfterService(1L, 10L);
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(afterServiceRepository.save(any(AfterService.class))).willReturn(saved);

        // when
        Long id = afterServiceService.create(request);

        // then
        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<AfterService> captor = ArgumentCaptor.forClass(AfterService.class);
        verify(afterServiceRepository).save(captor.capture());
        assertThat(captor.getValue().getReceiptNo()).isEqualTo("AS2026-0001");
    }

    @Test
    @DisplayName("create — 유상 판정은 청구액 유지")
    void create_paid_keeps_billing_amount() {
        // given
        AfterServiceCreateRequest request = new AfterServiceCreateRequest(
                null, 1L, 10L, LocalDate.of(2026, 5, 1),
                ServiceType.REPAIR, "레이저 출력 저하", ServiceStatus.RECEIVED,
                null, WarrantyDecision.PAID, 500_000L, null
        );
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(equipmentApi.getById(10L)).willReturn(equipmentInfo(1L));
        given(codeRuleApi.getRule(CodeRuleTarget.AFTER_SERVICE)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.AFTER_SERVICE), any())).willReturn("AS2026-0001");
        given(afterServiceRepository.existsByReceiptNo("AS2026-0001")).willReturn(false);
        given(afterServiceRepository.save(any(AfterService.class))).willReturn(mockAfterService(1L, 10L));

        // when
        afterServiceService.create(request);

        // then
        ArgumentCaptor<AfterService> captor = ArgumentCaptor.forClass(AfterService.class);
        verify(afterServiceRepository).save(captor.capture());
        assertThat(captor.getValue().getBillingAmount()).isEqualTo(500_000L);
    }

    @Test
    @DisplayName("create — 무상 판정은 청구액을 null 로 정규화")
    void create_free_nullifies_billing_amount() {
        // given
        AfterServiceCreateRequest request = new AfterServiceCreateRequest(
                null, 1L, 10L, LocalDate.of(2026, 5, 1),
                ServiceType.REPAIR, "레이저 출력 저하", ServiceStatus.RECEIVED,
                null, WarrantyDecision.FREE, 500_000L, null
        );
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(equipmentApi.getById(10L)).willReturn(equipmentInfo(1L));
        given(codeRuleApi.getRule(CodeRuleTarget.AFTER_SERVICE)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.AFTER_SERVICE), any())).willReturn("AS2026-0001");
        given(afterServiceRepository.existsByReceiptNo("AS2026-0001")).willReturn(false);
        given(afterServiceRepository.save(any(AfterService.class))).willReturn(mockAfterService(1L, 10L));

        // when
        afterServiceService.create(request);

        // then
        ArgumentCaptor<AfterService> captor = ArgumentCaptor.forClass(AfterService.class);
        verify(afterServiceRepository).save(captor.capture());
        assertThat(captor.getValue().getBillingAmount()).isNull();
    }

    @Test
    @DisplayName("create 실패 — 다른 고객사의 설비 연결 시 EQUIPMENT_CUSTOMER_MISMATCH")
    void create_fail_equipment_customer_mismatch() {
        // given
        AfterServiceCreateRequest request = baseCreateRequest(10L);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(equipmentApi.getById(10L)).willReturn(equipmentInfo(999L));

        // when & then
        assertThatThrownBy(() -> afterServiceService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.EQUIPMENT_CUSTOMER_MISMATCH);
        verify(afterServiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — 중복 접수번호 시 DUPLICATE_RECEIPT_NO")
    void create_fail_duplicate_receipt_no() {
        // given
        AfterServiceCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(codeRuleApi.getRule(CodeRuleTarget.AFTER_SERVICE)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.AFTER_SERVICE), any())).willReturn("AS2026-0001");
        given(afterServiceRepository.existsByReceiptNo("AS2026-0001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> afterServiceService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.DUPLICATE_RECEIPT_NO);
        verify(afterServiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("update 성공 — 필드 반영")
    void update_success() {
        // given
        AfterService as = mockAfterService(1L, null);
        given(afterServiceRepository.findById(1L)).willReturn(Optional.of(as));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        var request = new io.github.ladium1.erp.afterservice.internal.dto.AfterServiceUpdateRequest(
                1L, null, LocalDate.of(2026, 5, 1), ServiceType.REPAIR, "출력 저하",
                ServiceStatus.COMPLETED, null, WarrantyDecision.FREE, null, LocalDate.of(2026, 5, 3)
        );

        // when
        afterServiceService.update(1L, request);

        // then
        assertThat(as.getStatus()).isEqualTo(ServiceStatus.COMPLETED);
        assertThat(as.getWarrantyDecision()).isEqualTo(WarrantyDecision.FREE);
        assertThat(as.getCompletedDate()).isEqualTo(LocalDate.of(2026, 5, 3));
    }

    @Test
    @DisplayName("delete 성공 — 일지 / 경비도 함께 삭제")
    void delete_success() {
        // given
        given(afterServiceRepository.existsById(1L)).willReturn(true);

        // when
        afterServiceService.delete(1L);

        // then
        verify(visitRepository).deleteByAfterServiceId(1L);
        verify(expenseRepository).deleteByAfterServiceId(1L);
        verify(afterServiceRepository).deleteById(1L);
    }

    @Test
    @DisplayName("createVisit 성공 — 엔지니어 존재 검증 후 저장")
    void create_visit_success() {
        // given
        given(afterServiceRepository.existsById(1L)).willReturn(true);
        ServiceVisit saved = ServiceVisit.builder()
                .afterServiceId(1L).visitDate(LocalDate.of(2026, 5, 2)).engineerId(5L).build();
        ReflectionTestUtils.setField(saved, "id", 10L);
        given(visitRepository.save(any(ServiceVisit.class))).willReturn(saved);

        // when
        Long id = afterServiceService.createVisit(1L,
                new ServiceVisitRequest(LocalDate.of(2026, 5, 2), 5L, "출력 저하", "보호창 교체"));

        // then
        assertThat(id).isEqualTo(10L);
        verify(engineerService).validateId(5L);
    }

    @Test
    @DisplayName("createVisit 실패 — 존재하지 않는 AS 건")
    void create_visit_fail_after_service_not_found() {
        given(afterServiceRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> afterServiceService.createVisit(99L,
                new ServiceVisitRequest(LocalDate.of(2026, 5, 2), 5L, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.AFTER_SERVICE_NOT_FOUND);
        verify(visitRepository, never()).save(any());
    }

    @Test
    @DisplayName("createExpense 성공 — 엔지니어 무관 경비는 엔지니어 검증 생략")
    void create_expense_success_without_engineer() {
        // given
        given(afterServiceRepository.existsById(1L)).willReturn(true);
        ServiceExpense saved = expense(1L, ServiceExpenseCategory.PARTS, 1_200_000L);
        ReflectionTestUtils.setField(saved, "id", 20L);
        given(expenseRepository.save(any(ServiceExpense.class))).willReturn(saved);

        // when
        Long id = afterServiceService.createExpense(1L, new ServiceExpenseRequest(
                ServiceExpenseCategory.PARTS, 1_200_000L, ExpensePayerType.COMPANY, null, null, "보호창"));

        // then
        assertThat(id).isEqualTo(20L);
        verify(engineerService, never()).validateId(any());
    }

    @Test
    @DisplayName("updateExpense 실패 — EXPENSE_NOT_FOUND")
    void update_expense_fail_not_found() {
        given(expenseRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> afterServiceService.updateExpense(99L, new ServiceExpenseRequest(
                ServiceExpenseCategory.MEAL, 30_000L, ExpensePayerType.ENGINEER, null, 5L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.EXPENSE_NOT_FOUND);
    }

    @Test
    @DisplayName("typeStats — 건수 0 유형도 enum 순서 그대로 포함")
    void type_stats_includes_zero_types() {
        // given
        given(afterServiceRepository.countByTypeSince(any(LocalDate.class)))
                .willReturn(Map.of(ServiceType.REPAIR, 3L));
        given(afterServiceRepository.expenseSumByTypeSince(any(LocalDate.class)))
                .willReturn(Map.of(ServiceType.REPAIR, 850_000L));

        // when
        List<ServiceTypeStat> stats = afterServiceService.typeStats(6);

        // then
        assertThat(stats).hasSize(ServiceType.values().length);
        assertThat(stats.getFirst().type()).isEqualTo("REPAIR");
        assertThat(stats.getFirst().count()).isEqualTo(3);
        assertThat(stats.getFirst().expenseTotal()).isEqualTo(850_000L);
        assertThat(stats.getLast().count()).isZero();
    }

    @Test
    @DisplayName("engineerExpenseStats — 원가 내림차순 + 이름 enrich")
    void engineer_expense_stats_sorted_desc() {
        // given
        given(afterServiceRepository.expenseSumByEngineerSince(any(LocalDate.class)))
                .willReturn(Map.of(5L, 850_000L, 6L, 1_500_000L));
        given(engineerService.findNamesByIds(anyList()))
                .willReturn(Map.of(5L, "문영테크 김기사", 6L, "박기술"));

        // when
        List<EngineerExpenseStat> stats = afterServiceService.engineerExpenseStats(6);

        // then
        assertThat(stats).hasSize(2);
        assertThat(stats.getFirst().engineerName()).isEqualTo("박기술");
        assertThat(stats.getFirst().expenseTotal()).isEqualTo(1_500_000L);
        assertThat(stats.getLast().expenseTotal()).isEqualTo(850_000L);
    }

    private void stubRefNames() {
        given(customerApi.findByIds(anyList())).willReturn(List.of(customerInfo()));
        given(equipmentApi.findByIds(anyList())).willReturn(List.of(equipmentInfo(1L)));
        given(productApi.findByIds(anyList())).willReturn(List.of(
                ProductInfo.builder().id(3L).modelName("HLA-1530").supplierId(7L).active(true).build()));
        given(engineerService.findNamesByIds(anyList())).willReturn(Map.of(5L, "문영테크 김기사"));
    }

    private AfterService mockAfterService(Long customerId, Long equipmentId) {
        return AfterService.builder()
                .receiptNo("AS2026-0001")
                .customerId(customerId)
                .equipmentId(equipmentId)
                .receivedDate(LocalDate.of(2026, 5, 1))
                .type(ServiceType.REPAIR)
                .symptom("레이저 출력 저하")
                .status(ServiceStatus.ASSIGNED)
                .assignedEngineerId(5L)
                .warrantyDecision(WarrantyDecision.UNDECIDED)
                .build();
    }

    private ServiceExpense expense(Long afterServiceId, ServiceExpenseCategory category, Long amount) {
        return ServiceExpense.builder()
                .afterServiceId(afterServiceId)
                .category(category)
                .amount(amount)
                .payerType(ExpensePayerType.COMPANY)
                .build();
    }

    private AfterServiceCreateRequest baseCreateRequest(Long equipmentId) {
        return new AfterServiceCreateRequest(
                null, 1L, equipmentId, LocalDate.of(2026, 5, 1),
                ServiceType.REPAIR, "레이저 출력 저하", ServiceStatus.RECEIVED,
                null, WarrantyDecision.UNDECIDED, null, null
        );
    }

    private AfterServiceSearchCondition emptyCondition() {
        return new AfterServiceSearchCondition(null, null, null, null, null, null, null, null);
    }

    private CustomerInfo customerInfo() {
        return CustomerInfo.builder().id(1L).code("C0001").name("대성상사").build();
    }

    private EquipmentInfo equipmentInfo(Long customerId) {
        return EquipmentInfo.builder()
                .id(10L).customerId(customerId).productId(3L).serialNo("SN-001")
                .oscillatorWarrantyEndDate(LocalDate.of(2029, 3, 2))
                .generalWarrantyEndDate(LocalDate.of(2027, 3, 2))
                .build();
    }

    private CodeRuleInfo ruleWithMode(InputMode mode) {
        return CodeRuleInfo.builder()
                .target(CodeRuleTarget.AFTER_SERVICE)
                .pattern("AS{YYYY}-{SEQ:4}")
                .inputMode(mode)
                .description("테스트")
                .build();
    }
}
