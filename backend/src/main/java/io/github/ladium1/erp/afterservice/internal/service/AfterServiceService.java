package io.github.ladium1.erp.afterservice.internal.service;

import io.github.ladium1.erp.afterservice.api.AfterServiceApi;
import io.github.ladium1.erp.afterservice.api.dto.EngineerExpenseStat;
import io.github.ladium1.erp.afterservice.api.dto.ServiceTypeStat;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceCreateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceDetailResponse;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceExcelRow;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSummaryResponse;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceUpdateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceExpenseRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceExpenseResponse;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceVisitRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceVisitResponse;
import io.github.ladium1.erp.afterservice.internal.entity.AfterService;
import io.github.ladium1.erp.afterservice.internal.entity.AfterServiceProcessPolicy;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceExpense;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceVisit;
import io.github.ladium1.erp.afterservice.internal.excel.AfterServiceExcelExporter;
import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.afterservice.internal.repository.AfterServiceRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceExpenseRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceVisitRepository;
import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.equipment.api.EquipmentApi;
import io.github.ladium1.erp.equipment.api.dto.EquipmentInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.demo.DemoExcelExportGuard;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.validation.MoneyPolicy;
import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.product.api.dto.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AfterServiceService implements AfterServiceApi {

    private final AfterServiceRepository afterServiceRepository;
    private final ServiceVisitRepository visitRepository;
    private final ServiceExpenseRepository expenseRepository;
    private final EngineerService engineerService;
    private final AfterServiceExcelExporter excelExporter;
    private final CodeRuleApi codeRuleApi;
    private final CustomerApi customerApi;
    private final EquipmentApi equipmentApi;
    private final ProductApi productApi;
    private final DemoExcelExportGuard demoExcelExportGuard;

    public PageResponse<AfterServiceSummaryResponse> search(AfterServiceSearchCondition condition, Pageable pageable) {
        Page<AfterService> page = afterServiceRepository.search(condition, pageable);
        RefNames refs = loadRefNames(page.getContent());
        Map<Long, Long> expenseSums = expenseRepository.sumAmountByAfterServiceIds(
                page.getContent().stream().map(AfterService::getId).toList());
        return PageResponse.of(page.map(a -> toSummaryResponse(a, refs, expenseSums)));
    }

    /**
     * 검색 조건 + 정렬 그대로 전체 페이지를 .xlsx 바이트로 직렬화. 페이지네이션 무시 — 필터링된 전체.
     */
    public byte[] exportExcel(AfterServiceSearchCondition condition, Sort sort) {
        demoExcelExportGuard.assertExportAllowed(DemoExcelExportGuard.Table.AFTER_SERVICES);
        List<AfterService> services = afterServiceRepository.searchAll(condition, sort);
        RefNames refs = loadRefNames(services);
        Map<Long, Long> expenseSums = expenseRepository.sumAmountByAfterServiceIds(
                services.stream().map(AfterService::getId).toList());

        List<AfterServiceExcelRow> rows = services.stream()
                .map(a -> AfterServiceExcelRow.builder()
                        .receiptNo(a.getReceiptNo())
                        .customerName(refs.customer().get(a.getCustomerId()))
                        .equipmentModelName(refs.equipmentModelName(a.getEquipmentId()))
                        .equipmentSerialNo(refs.equipmentSerialNo(a.getEquipmentId()))
                        .type(a.getType())
                        .status(a.getStatus())
                        .receivedDate(a.getReceivedDate())
                        .completedDate(a.getCompletedDate())
                        .assignedEngineerName(a.getAssignedEngineerId() == null
                                ? null
                                : refs.engineer().get(a.getAssignedEngineerId()))
                        .warrantyDecision(a.getWarrantyDecision())
                        .billingAmount(a.getBillingAmount())
                        .expenseTotal(expenseSums.getOrDefault(a.getId(), 0L))
                        .symptom(a.getSymptom())
                        .build())
                .toList();
        return excelExporter.export(rows);
    }

    public AfterServiceDetailResponse getDetail(Long id) {
        AfterService afterService = afterServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AfterServiceErrorCode.AFTER_SERVICE_NOT_FOUND));
        RefNames refs = loadRefNames(List.of(afterService));

        List<ServiceVisit> visits = visitRepository.findByAfterServiceIdOrderByVisitDateDescIdDesc(id);
        List<ServiceExpense> expenses = expenseRepository.findByAfterServiceIdOrderByIdAsc(id);
        Map<Long, String> engineerNames = engineerService.findNamesByIds(collectEngineerIds(afterService, visits, expenses));

        long expenseTotal = expenses.stream()
                .map(ServiceExpense::getAmount)
                .reduce(0L, MoneyPolicy::addExact);

        return AfterServiceDetailResponse.builder()
                .id(afterService.getId())
                .receiptNo(afterService.getReceiptNo())
                .customerId(afterService.getCustomerId())
                .customerName(refs.customer().get(afterService.getCustomerId()))
                .equipmentId(afterService.getEquipmentId())
                .equipmentModelName(refs.equipmentModelName(afterService.getEquipmentId()))
                .equipmentSerialNo(refs.equipmentSerialNo(afterService.getEquipmentId()))
                .receivedDate(afterService.getReceivedDate())
                .type(afterService.getType())
                .symptom(afterService.getSymptom())
                .status(afterService.getStatus())
                .assignedEngineerId(afterService.getAssignedEngineerId())
                .assignedEngineerName(afterService.getAssignedEngineerId() == null
                        ? null
                        : engineerNames.get(afterService.getAssignedEngineerId()))
                .warrantyDecision(afterService.getWarrantyDecision())
                .billingAmount(afterService.getBillingAmount())
                .completedDate(afterService.getCompletedDate())
                .expenseTotal(expenseTotal)
                .visits(visits.stream()
                        .map(v -> ServiceVisitResponse.builder()
                                .id(v.getId())
                                .visitDate(v.getVisitDate())
                                .engineerId(v.getEngineerId())
                                .engineerName(engineerNames.get(v.getEngineerId()))
                                .problem(v.getProblem())
                                .resolution(v.getResolution())
                                .build())
                        .toList())
                .expenses(expenses.stream()
                        .map(e -> ServiceExpenseResponse.builder()
                                .id(e.getId())
                                .category(e.getCategory())
                                .amount(e.getAmount())
                                .payerType(e.getPayerType())
                                .paidDate(e.getPaidDate())
                                .engineerId(e.getEngineerId())
                                .engineerName(e.getEngineerId() == null ? null : engineerNames.get(e.getEngineerId()))
                                .note(e.getNote())
                                .build())
                        .toList())
                .build();
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.CREATE, targetType = "AfterService", targetIdFromReturn = true)
    @Transactional
    public Long create(AfterServiceCreateRequest request) {
        AfterServiceProcessPolicy.validate(
                request.status(),
                request.receivedDate(),
                request.completedDate(),
                request.warrantyDecision(),
                request.billingAmount()
        );
        validateReferences(request.customerId(), request.equipmentId(), request.assignedEngineerId(), null);

        String receiptNo = resolveReceiptNo(request.receiptNo());
        if (afterServiceRepository.existsByReceiptNo(receiptNo)) {
            throw new BusinessException(AfterServiceErrorCode.DUPLICATE_RECEIPT_NO);
        }

        AfterService afterService = AfterService.builder()
                .receiptNo(receiptNo)
                .customerId(request.customerId())
                .equipmentId(request.equipmentId())
                .receivedDate(request.receivedDate())
                .type(request.type())
                .symptom(request.symptom())
                .status(request.status())
                .assignedEngineerId(request.assignedEngineerId())
                .warrantyDecision(request.warrantyDecision())
                .billingAmount(request.billingAmount())
                .completedDate(request.completedDate())
                .build();
        return afterServiceRepository.save(afterService).getId();
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.UPDATE, targetType = "AfterService", targetIdParam = "id")
    @Transactional
    public void update(Long id, AfterServiceUpdateRequest request) {
        AfterService afterService = afterServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AfterServiceErrorCode.AFTER_SERVICE_NOT_FOUND));
        AfterServiceProcessPolicy.validate(
                request.status(),
                request.receivedDate(),
                request.completedDate(),
                request.warrantyDecision(),
                request.billingAmount()
        );
        validateReferences(
                request.customerId(),
                request.equipmentId(),
                request.assignedEngineerId(),
                afterService.getAssignedEngineerId()
        );

        afterService.update(
                request.customerId(),
                request.equipmentId(),
                request.receivedDate(),
                request.type(),
                request.symptom(),
                request.status(),
                request.assignedEngineerId(),
                request.warrantyDecision(),
                request.billingAmount(),
                request.completedDate()
        );
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.DELETE, targetType = "AfterService", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        if (!afterServiceRepository.existsById(id)) {
            throw new BusinessException(AfterServiceErrorCode.AFTER_SERVICE_NOT_FOUND);
        }
        // 방문 일지 / 경비도 함께 제거 (물리 삭제 — AS 건 자체를 지우는 케이스)
        visitRepository.deleteByAfterServiceId(id);
        expenseRepository.deleteByAfterServiceId(id);
        afterServiceRepository.deleteById(id);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.DELETE, targetType = "AfterService")
    @Transactional
    public void deleteAll(List<Long> ids) {
        RequestCollectionPolicy.requireBoundedMutationBatch(ids);
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.CREATE, targetType = "ServiceVisit", targetIdFromReturn = true)
    @Transactional
    public Long createVisit(Long afterServiceId, ServiceVisitRequest request) {
        requireAfterService(afterServiceId);
        engineerService.validateWorkReference(request.engineerId(), null);

        ServiceVisit visit = ServiceVisit.builder()
                .afterServiceId(afterServiceId)
                .visitDate(request.visitDate())
                .engineerId(request.engineerId())
                .problem(request.problem())
                .resolution(request.resolution())
                .build();
        return visitRepository.save(visit).getId();
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.UPDATE, targetType = "ServiceVisit", targetIdParam = "id")
    @Transactional
    public void updateVisit(Long id, ServiceVisitRequest request) {
        ServiceVisit visit = visitRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AfterServiceErrorCode.VISIT_NOT_FOUND));
        engineerService.validateWorkReference(request.engineerId(), visit.getEngineerId());
        visit.update(request.visitDate(), request.engineerId(), request.problem(), request.resolution());
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.DELETE, targetType = "ServiceVisit", targetIdParam = "id")
    @Transactional
    public void deleteVisit(Long id) {
        if (!visitRepository.existsById(id)) {
            throw new BusinessException(AfterServiceErrorCode.VISIT_NOT_FOUND);
        }
        visitRepository.deleteById(id);
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.CREATE, targetType = "ServiceExpense", targetIdFromReturn = true)
    @Transactional
    public Long createExpense(Long afterServiceId, ServiceExpenseRequest request) {
        requireAfterService(afterServiceId);
        if (request.engineerId() != null) {
            engineerService.validateWorkReference(request.engineerId(), null);
        }

        ServiceExpense expense = ServiceExpense.builder()
                .afterServiceId(afterServiceId)
                .category(request.category())
                .amount(request.amount())
                .payerType(request.payerType())
                .paidDate(request.paidDate())
                .engineerId(request.engineerId())
                .note(request.note())
                .build();
        return expenseRepository.save(expense).getId();
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.UPDATE, targetType = "ServiceExpense", targetIdParam = "id")
    @Transactional
    public void updateExpense(Long id, ServiceExpenseRequest request) {
        ServiceExpense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AfterServiceErrorCode.EXPENSE_NOT_FOUND));
        if (request.engineerId() != null) {
            engineerService.validateWorkReference(request.engineerId(), expense.getEngineerId());
        }
        expense.update(
                request.category(),
                request.amount(),
                request.payerType(),
                request.paidDate(),
                request.engineerId(),
                request.note()
        );
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.DELETE, targetType = "ServiceExpense", targetIdParam = "id")
    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new BusinessException(AfterServiceErrorCode.EXPENSE_NOT_FOUND);
        }
        expenseRepository.deleteById(id);
    }

    @Override
    public List<ServiceTypeStat> typeStats(int months) {
        java.time.LocalDate fromDate = java.time.YearMonth.now().minusMonths(months - 1L).atDay(1);
        Map<ServiceType, Long> counts = afterServiceRepository.countByTypeSince(fromDate);
        Map<ServiceType, Long> expenseSums = afterServiceRepository.expenseSumByTypeSince(fromDate);

        // 건수 0 유형도 노출 — 위젯에서 유형 축이 고정되도록 enum 순서 그대로.
        return java.util.Arrays.stream(ServiceType.values())
                .map(type -> ServiceTypeStat.builder()
                        .type(type.name())
                        .typeLabel(type.getDescription())
                        .count(counts.getOrDefault(type, 0L))
                        .expenseTotal(expenseSums.getOrDefault(type, 0L))
                        .build())
                .toList();
    }

    @Override
    public List<EngineerExpenseStat> engineerExpenseStats(int months) {
        java.time.LocalDate fromDate = java.time.YearMonth.now().minusMonths(months - 1L).atDay(1);
        Map<Long, Long> sums = afterServiceRepository.expenseSumByEngineerSince(fromDate);
        Map<Long, String> names = engineerService.findNamesByIds(sums.keySet().stream().toList());

        return sums.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(entry -> EngineerExpenseStat.builder()
                        .engineerId(entry.getKey())
                        .engineerName(names.get(entry.getKey()))
                        .expenseTotal(entry.getValue())
                        .build())
                .toList();
    }

    /**
     * 참조 무결성 검증 — 고객사 / 설비 / 주 담당 엔지니어.
     * 설비가 연결되면 그 설비의 소속 고객사와 접수 고객사가 일치해야 한다 (남의 설비로 접수 방지).
     */
    private void validateReferences(
            Long customerId,
            Long equipmentId,
            Long assignedEngineerId,
            Long currentEngineerId
    ) {
        customerApi.getById(customerId);
        if (equipmentId != null) {
            EquipmentInfo equipment = equipmentApi.getById(equipmentId);
            if (!Objects.equals(equipment.customerId(), customerId)) {
                throw new BusinessException(AfterServiceErrorCode.EQUIPMENT_CUSTOMER_MISMATCH);
            }
        }
        if (assignedEngineerId != null) {
            engineerService.validateWorkReference(assignedEngineerId, currentEngineerId);
        }
    }

    private void requireAfterService(Long afterServiceId) {
        if (!afterServiceRepository.existsById(afterServiceId)) {
            throw new BusinessException(AfterServiceErrorCode.AFTER_SERVICE_NOT_FOUND);
        }
    }

    private AfterServiceSummaryResponse toSummaryResponse(AfterService a, RefNames refs, Map<Long, Long> expenseSums) {
        return AfterServiceSummaryResponse.builder()
                .id(a.getId())
                .receiptNo(a.getReceiptNo())
                .customerId(a.getCustomerId())
                .customerName(refs.customer().get(a.getCustomerId()))
                .equipmentId(a.getEquipmentId())
                .equipmentModelName(refs.equipmentModelName(a.getEquipmentId()))
                .equipmentSerialNo(refs.equipmentSerialNo(a.getEquipmentId()))
                .receivedDate(a.getReceivedDate())
                .type(a.getType())
                .status(a.getStatus())
                .assignedEngineerId(a.getAssignedEngineerId())
                .assignedEngineerName(a.getAssignedEngineerId() == null
                        ? null
                        : refs.engineer().get(a.getAssignedEngineerId()))
                .warrantyDecision(a.getWarrantyDecision())
                .billingAmount(a.getBillingAmount())
                .expenseTotal(expenseSums.getOrDefault(a.getId(), 0L))
                .completedDate(a.getCompletedDate())
                .build();
    }

    /**
     * 접수번호 채번 — 채번 규칙의 inputMode 에 따라 최종 번호 결정.
     * AUTO: 항상 시스템 생성 / MANUAL: 사용자 입력 필수 + 패턴 검증 / AUTO_OR_MANUAL: 입력 있으면 검증, 없으면 생성.
     */
    private String resolveReceiptNo(String requested) {
        CodeRuleInfo rule = codeRuleApi.getRule(CodeRuleTarget.AFTER_SERVICE);
        InputMode mode = rule.inputMode();
        boolean hasInput = requested != null && !requested.isBlank();

        if (mode == InputMode.AUTO || (mode == InputMode.AUTO_OR_MANUAL && !hasInput)) {
            return codeRuleApi.generate(CodeRuleTarget.AFTER_SERVICE, CodeGenerationContext.empty());
        }
        if (!hasInput) {
            throw new BusinessException(AfterServiceErrorCode.RECEIPT_NO_REQUIRED);
        }
        String trimmed = requested.trim();
        codeRuleApi.validate(CodeRuleTarget.AFTER_SERVICE, trimmed);
        return trimmed;
    }

    private List<Long> collectEngineerIds(AfterService afterService,
                                          List<ServiceVisit> visits,
                                          List<ServiceExpense> expenses) {
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(afterService.getAssignedEngineerId()),
                        java.util.stream.Stream.concat(
                                visits.stream().map(ServiceVisit::getEngineerId),
                                expenses.stream().map(ServiceExpense::getEngineerId)))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 참조 이름 일괄 enrichment — 목록 / 상세 / 엑셀이 공유.
     * 설비 표시는 설비 → 제품 모델명 2단 조회 (설비 대장은 모델명을 중복 저장하지 않는다).
     */
    private RefNames loadRefNames(List<AfterService> services) {
        if (services.isEmpty()) {
            return new RefNames(Map.of(), Map.of(), Map.of(), Map.of());
        }
        Map<Long, String> customers = customerApi.findByIds(
                        services.stream().map(AfterService::getCustomerId).distinct().toList()).stream()
                .collect(toMap(CustomerInfo::id, CustomerInfo::name));

        Map<Long, EquipmentInfo> equipments = equipmentApi.findByIds(
                        services.stream().map(AfterService::getEquipmentId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .collect(toMap(EquipmentInfo::id, e -> e));

        Map<Long, String> productNames = productApi.findByIds(
                        equipments.values().stream().map(EquipmentInfo::productId).distinct().toList()).stream()
                .collect(toMap(ProductInfo::id, ProductInfo::modelName));

        Map<Long, String> engineers = engineerService.findNamesByIds(
                services.stream().map(AfterService::getAssignedEngineerId).filter(Objects::nonNull).distinct().toList());

        return new RefNames(customers, equipments, productNames, engineers);
    }

    private record RefNames(Map<Long, String> customer,
                            Map<Long, EquipmentInfo> equipment,
                            Map<Long, String> productName,
                            Map<Long, String> engineer) {

        String equipmentModelName(Long equipmentId) {
            if (equipmentId == null) return null;
            EquipmentInfo info = equipment.get(equipmentId);
            return info == null ? null : productName.get(info.productId());
        }

        String equipmentSerialNo(Long equipmentId) {
            if (equipmentId == null) return null;
            EquipmentInfo info = equipment.get(equipmentId);
            return info == null ? null : info.serialNo();
        }
    }
}
