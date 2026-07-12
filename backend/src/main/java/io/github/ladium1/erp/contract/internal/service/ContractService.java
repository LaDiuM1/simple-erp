package io.github.ladium1.erp.contract.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.contract.internal.dto.ContractCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractDetailResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractExcelRow;
import io.github.ladium1.erp.contract.internal.dto.ContractNoteCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractNoteResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractPaymentRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.dto.ContractSummaryResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractUpdateRequest;
import io.github.ladium1.erp.contract.internal.entity.Contract;
import io.github.ladium1.erp.contract.internal.entity.ContractNote;
import io.github.ladium1.erp.contract.internal.entity.ContractPayment;
import io.github.ladium1.erp.contract.internal.excel.ContractExcelExporter;
import io.github.ladium1.erp.contract.internal.exception.ContractErrorCode;
import io.github.ladium1.erp.contract.internal.mapper.ContractMapper;
import io.github.ladium1.erp.contract.internal.repository.ContractNoteRepository;
import io.github.ladium1.erp.contract.internal.repository.ContractPaymentRepository;
import io.github.ladium1.erp.contract.internal.repository.ContractRepository;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.security.DataScopeResolver;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.product.api.dto.ProductInfo;
import io.github.ladium1.erp.supplier.api.SupplierApi;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractPaymentRepository paymentRepository;
    private final ContractNoteRepository noteRepository;
    private final ContractMapper contractMapper;
    private final ContractExcelExporter excelExporter;
    private final CodeRuleApi codeRuleApi;
    private final CustomerApi customerApi;
    private final EmployeeApi employeeApi;
    private final SupplierApi supplierApi;
    private final ProductApi productApi;
    private final DataScopeResolver dataScopeResolver;
    private final DataScopeContextProvider dataScopeContextProvider;

    public PageResponse<ContractSummaryResponse> search(ContractSearchCondition condition, Pageable pageable) {
        Optional<Set<Long>> visible = resolveVisibleEmployeeIds();
        if (visible.isPresent() && visible.get().isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        ContractSearchCondition scoped = visible.map(condition::withEmployeeIdScope).orElse(condition);
        Page<Contract> page = contractRepository.search(scoped, pageable);

        RefNames refs = loadRefNames(page.getContent());
        Map<Long, Long> paidSums = paymentRepository.sumPaidAmountByContractIds(
                page.getContent().stream().map(Contract::getId).toList());

        return PageResponse.of(page.map(c -> toSummaryResponse(c, refs, paidSums)));
    }

    /**
     * 검색 조건 + 정렬 그대로 전체 페이지를 .xlsx 바이트로 직렬화. 페이지네이션 무시 — 필터링된 전체.
     */
    public byte[] exportExcel(ContractSearchCondition condition, Sort sort) {
        Optional<Set<Long>> visible = resolveVisibleEmployeeIds();
        if (visible.isPresent() && visible.get().isEmpty()) {
            return excelExporter.export(List.of());
        }
        ContractSearchCondition scoped = visible.map(condition::withEmployeeIdScope).orElse(condition);
        List<Contract> contracts = contractRepository.searchAll(scoped, sort);

        RefNames refs = loadRefNames(contracts);
        Map<Long, Long> paidSums = paymentRepository.sumPaidAmountByContractIds(
                contracts.stream().map(Contract::getId).toList());

        List<ContractExcelRow> rows = contracts.stream()
                .map(c -> {
                    Long paidTotal = paidSums.getOrDefault(c.getId(), 0L);
                    ProductInfo product = refs.product().get(c.getProductId());
                    return ContractExcelRow.builder()
                            .contractNo(c.getContractNo())
                            .customerName(refs.customer().get(c.getCustomerId()))
                            .employeeName(refs.employee().get(c.getEmployeeId()))
                            .supplierName(refs.supplier().get(c.getSupplierId()))
                            .categoryName(product == null ? null : product.categoryName())
                            .productModelName(product == null ? null : product.modelName())
                            .outputValue(c.getOutputValue())
                            .outputUnit(c.getOutputUnit())
                            .optionText(c.getOptionText())
                            .cretopGrade(c.getCretopGrade())
                            .supportProgramName(c.getSupportProgramName())
                            .supportProgramStatus(c.getSupportProgramStatus())
                            .status(c.getStatus())
                            .contractDate(c.getContractDate())
                            .dueDate(c.getDueDate())
                            .orderDate(c.getOrderDate())
                            .expectedArrivalDate(c.getExpectedArrivalDate())
                            .arrivalDate(c.getArrivalDate())
                            .installedDate(c.getInstalledDate())
                            .settledDate(c.getSettledDate())
                            .initialAmount(c.getInitialAmount())
                            .finalAmount(c.getFinalAmount())
                            .paidTotal(paidTotal)
                            .outstandingAmount(outstanding(c.getFinalAmount(), paidTotal))
                            .logisticsNote(c.getLogisticsNote())
                            .build();
                })
                .toList();
        return excelExporter.export(rows);
    }

    public ContractDetailResponse getDetail(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND));
        assertVisible(contract);

        RefNames refs = loadRefNames(List.of(contract));
        ProductInfo product = refs.product().get(contract.getProductId());

        List<ContractPayment> payments = paymentRepository.findByContractIdOrderByIdAsc(id);
        long paidTotal = payments.stream()
                .map(ContractPayment::getPaidAmount)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        List<ContractNote> notes = noteRepository.findByContractIdOrderByIdDesc(id);
        Map<Long, String> authorNames = loadEmployeeNames(
                notes.stream().map(ContractNote::getAuthorEmployeeId).distinct().toList());

        return ContractDetailResponse.builder()
                .id(contract.getId())
                .contractNo(contract.getContractNo())
                .customerId(contract.getCustomerId())
                .customerName(refs.customer().get(contract.getCustomerId()))
                .employeeId(contract.getEmployeeId())
                .employeeName(refs.employee().get(contract.getEmployeeId()))
                .supplierId(contract.getSupplierId())
                .supplierName(refs.supplier().get(contract.getSupplierId()))
                .productId(contract.getProductId())
                .productModelName(product == null ? null : product.modelName())
                .categoryName(product == null ? null : product.categoryName())
                .outputValue(contract.getOutputValue())
                .outputUnit(contract.getOutputUnit())
                .optionText(contract.getOptionText())
                .initialAmount(contract.getInitialAmount())
                .finalAmount(contract.getFinalAmount())
                .paidTotal(paidTotal)
                .outstandingAmount(outstanding(contract.getFinalAmount(), paidTotal))
                .cretopGrade(contract.getCretopGrade())
                .supportProgramName(contract.getSupportProgramName())
                .supportProgramStatus(contract.getSupportProgramStatus())
                .contractDate(contract.getContractDate())
                .dueDate(contract.getDueDate())
                .orderDate(contract.getOrderDate())
                .expectedArrivalDate(contract.getExpectedArrivalDate())
                .arrivalDate(contract.getArrivalDate())
                .installedDate(contract.getInstalledDate())
                .settledDate(contract.getSettledDate())
                .logisticsNote(contract.getLogisticsNote())
                .status(contract.getStatus())
                .payments(payments.stream().map(contractMapper::toPaymentResponse).toList())
                .notes(notes.stream()
                        .map(n -> contractMapper.toNoteResponse(n, authorNames.get(n.getAuthorEmployeeId())))
                        .toList())
                .build();
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.CREATE, targetType = "Contract", targetIdFromReturn = true)
    @Transactional
    public Long create(ContractCreateRequest request) {
        // 참조 존재 검증 — 없으면 각 모듈이 NOT_FOUND 를 던진다.
        customerApi.getById(request.customerId());
        employeeApi.getById(request.employeeId());
        ProductInfo product = productApi.getById(request.productId());

        String contractNo = resolveContractNo(request.contractNo());
        if (contractRepository.existsByContractNo(contractNo)) {
            throw new BusinessException(ContractErrorCode.DUPLICATE_CONTRACT_NO);
        }

        Contract contract = Contract.builder()
                .contractNo(contractNo)
                .customerId(request.customerId())
                .employeeId(request.employeeId())
                .supplierId(product.supplierId())
                .productId(request.productId())
                .outputValue(request.outputValue())
                .outputUnit(request.outputUnit())
                .optionText(request.optionText())
                .initialAmount(request.initialAmount())
                .finalAmount(request.finalAmount())
                .cretopGrade(request.cretopGrade())
                .supportProgramName(request.supportProgramName())
                .supportProgramStatus(request.supportProgramStatus())
                .contractDate(request.contractDate())
                .dueDate(request.dueDate())
                .orderDate(request.orderDate())
                .expectedArrivalDate(request.expectedArrivalDate())
                .arrivalDate(request.arrivalDate())
                .installedDate(request.installedDate())
                .settledDate(request.settledDate())
                .logisticsNote(request.logisticsNote())
                .status(request.status())
                .build();

        return contractRepository.save(contract).getId();
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.UPDATE, targetType = "Contract", targetIdParam = "id")
    @Transactional
    public void update(Long id, ContractUpdateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND));
        assertVisible(contract);

        customerApi.getById(request.customerId());
        employeeApi.getById(request.employeeId());
        ProductInfo product = productApi.getById(request.productId());

        contract.update(
                request.customerId(),
                request.employeeId(),
                product.supplierId(),
                request.productId(),
                request.outputValue(),
                request.outputUnit(),
                request.optionText(),
                request.initialAmount(),
                request.finalAmount(),
                request.cretopGrade(),
                request.supportProgramName(),
                request.supportProgramStatus(),
                request.contractDate(),
                request.dueDate(),
                request.orderDate(),
                request.expectedArrivalDate(),
                request.arrivalDate(),
                request.installedDate(),
                request.settledDate(),
                request.logisticsNote(),
                request.status()
        );
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.DELETE, targetType = "Contract", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND));
        assertVisible(contract);
        // 대금 스케줄 / 메모도 함께 제거 (물리 삭제 — 계약 자체를 지우는 케이스)
        paymentRepository.deleteByContractId(id);
        noteRepository.deleteByContractId(id);
        contractRepository.delete(contract);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.DELETE, targetType = "Contract")
    @Transactional
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.CREATE, targetType = "ContractPayment", targetIdFromReturn = true)
    @Transactional
    public Long createPayment(Long contractId, ContractPaymentRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND));
        assertVisible(contract);

        ContractPayment payment = ContractPayment.builder()
                .contractId(contract.getId())
                .label(request.label().trim())
                .plannedDate(request.plannedDate())
                .plannedAmount(request.plannedAmount())
                .paidDate(request.paidDate())
                .paidAmount(request.paidAmount())
                .invoiceDate(request.invoiceDate())
                .invoiceAmount(request.invoiceAmount())
                .note(request.note())
                .build();
        return paymentRepository.save(payment).getId();
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.UPDATE, targetType = "ContractPayment", targetIdParam = "id")
    @Transactional
    public void updatePayment(Long id, ContractPaymentRequest request) {
        ContractPayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.PAYMENT_NOT_FOUND));
        assertVisibleByContractId(payment.getContractId());

        payment.update(
                request.label().trim(),
                request.plannedDate(),
                request.plannedAmount(),
                request.paidDate(),
                request.paidAmount(),
                request.invoiceDate(),
                request.invoiceAmount(),
                request.note()
        );
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.DELETE, targetType = "ContractPayment", targetIdParam = "id")
    @Transactional
    public void deletePayment(Long id) {
        ContractPayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.PAYMENT_NOT_FOUND));
        assertVisibleByContractId(payment.getContractId());
        paymentRepository.delete(payment);
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.CREATE, targetType = "ContractNote", targetIdFromReturn = true)
    @Transactional
    public Long createNote(Long contractId, ContractNoteCreateRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND));
        assertVisible(contract);

        ContractNote note = ContractNote.builder()
                .contractId(contract.getId())
                .authorEmployeeId(currentEmployeeId())
                .content(request.content().trim())
                .build();
        return noteRepository.save(note).getId();
    }

    @Auditable(menu = Menu.CONTRACTS, action = AuditAction.DELETE, targetType = "ContractNote", targetIdParam = "id")
    @Transactional
    public void deleteNote(Long id) {
        ContractNote note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.NOTE_NOT_FOUND));
        assertVisibleByContractId(note.getContractId());
        noteRepository.delete(note);
    }

    private ContractSummaryResponse toSummaryResponse(Contract c, RefNames refs, Map<Long, Long> paidSums) {
        Long paidTotal = paidSums.getOrDefault(c.getId(), 0L);
        ProductInfo product = refs.product().get(c.getProductId());
        return ContractSummaryResponse.builder()
                .id(c.getId())
                .contractNo(c.getContractNo())
                .customerId(c.getCustomerId())
                .customerName(refs.customer().get(c.getCustomerId()))
                .employeeId(c.getEmployeeId())
                .employeeName(refs.employee().get(c.getEmployeeId()))
                .supplierId(c.getSupplierId())
                .supplierName(refs.supplier().get(c.getSupplierId()))
                .productId(c.getProductId())
                .productModelName(product == null ? null : product.modelName())
                .categoryName(product == null ? null : product.categoryName())
                .outputValue(c.getOutputValue())
                .outputUnit(c.getOutputUnit())
                .finalAmount(c.getFinalAmount())
                .outstandingAmount(outstanding(c.getFinalAmount(), paidTotal))
                .contractDate(c.getContractDate())
                .dueDate(c.getDueDate())
                .supportProgramName(c.getSupportProgramName())
                .supportProgramStatus(c.getSupportProgramStatus())
                .status(c.getStatus())
                .build();
    }

    /**
     * 채번 규칙의 inputMode 에 따라 최종 계약 번호 결정.
     * AUTO: 항상 시스템 생성 / MANUAL: 사용자 입력 필수 + 패턴 검증 / AUTO_OR_MANUAL: 입력 있으면 검증, 없으면 생성.
     */
    private String resolveContractNo(String requested) {
        CodeRuleInfo rule = codeRuleApi.getRule(CodeRuleTarget.CONTRACT);
        InputMode mode = rule.inputMode();
        boolean hasInput = requested != null && !requested.isBlank();

        if (mode == InputMode.AUTO || (mode == InputMode.AUTO_OR_MANUAL && !hasInput)) {
            return codeRuleApi.generate(CodeRuleTarget.CONTRACT, CodeGenerationContext.empty());
        }
        if (!hasInput) {
            throw new BusinessException(ContractErrorCode.CONTRACT_NO_REQUIRED);
        }
        String trimmed = requested.trim();
        codeRuleApi.validate(CodeRuleTarget.CONTRACT, trimmed);
        return trimmed;
    }

    private static Long outstanding(Long finalAmount, Long paidTotal) {
        if (finalAmount == null) {
            return null;
        }
        return finalAmount - (paidTotal == null ? 0L : paidTotal);
    }

    /**
     * 데이터 스코프 적용 후 가시 계약자 (employee) 식별자 집합을 산출.
     * 계약 가시성의 근거 (계약자 귀속) 가 이 모듈 자신의 employee_id 컬럼이라 customer 처럼
     * 외부 contributor SPI 를 두지 않고 스코프 → 직원 집합 변환만 수행한다.
     * - {@code Optional.empty()} = ALL (제한 없음)
     * - {@code Optional.of(empty)} = 보이는 행 0 건 (서비스가 빈 결과로 분기)
     * - {@code Optional.of(set)} = 그 계약자들의 계약만 보임
     */
    private Optional<Set<Long>> resolveVisibleEmployeeIds() {
        DataScope scope = dataScopeResolver.resolve(Menu.CONTRACTS);
        if (scope == DataScope.ALL) {
            return Optional.empty();
        }
        DataScopeContext ctx = dataScopeContextProvider.current();
        return Optional.of(switch (scope) {
            case ALL -> Set.of(); // 위에서 분기 — defensive
            case SELF -> ctx.employeeId() == null ? Set.of() : Set.of(ctx.employeeId());
            case DEPARTMENT -> ctx.departmentId() == null
                    ? Set.of()
                    : Set.copyOf(employeeApi.findIdsByDepartmentIds(List.of(ctx.departmentId())));
            case DEPARTMENT_TREE -> ctx.departmentSubtreeIds().isEmpty()
                    ? Set.of()
                    : Set.copyOf(employeeApi.findIdsByDepartmentIds(ctx.departmentSubtreeIds()));
        });
    }

    /**
     * 단건 가시성 강제 — 가시 집합에 없는 계약은 존재 자체를 노출하지 않기 위해 NOT_FOUND 로 처리.
     */
    private void assertVisible(Contract contract) {
        Optional<Set<Long>> visible = resolveVisibleEmployeeIds();
        if (visible.isPresent() && !visible.get().contains(contract.getEmployeeId())) {
            throw new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND);
        }
    }

    private void assertVisibleByContractId(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND));
        assertVisible(contract);
    }

    private Long currentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException(ContractErrorCode.AUTHOR_NOT_RESOLVED);
        }
        return employeeApi.findByLoginId(authentication.getName())
                .map(EmployeeInfo::id)
                .orElseThrow(() -> new BusinessException(ContractErrorCode.AUTHOR_NOT_RESOLVED));
    }

    /**
     * 참조 이름 일괄 enrichment — 목록 / 상세 / 엑셀이 공유.
     * 참조 대상이 지워진 경우 (직원 하드 삭제 등) 이름은 null 로 남기고 응답은 유지한다.
     */
    private RefNames loadRefNames(List<Contract> contracts) {
        if (contracts.isEmpty()) {
            return new RefNames(Map.of(), Map.of(), Map.of(), Map.of());
        }
        Map<Long, String> customers = customerApi.findByIds(
                        contracts.stream().map(Contract::getCustomerId).distinct().toList()).stream()
                .collect(toMap(CustomerInfo::id, CustomerInfo::name));
        Map<Long, String> employees = loadEmployeeNames(
                contracts.stream().map(Contract::getEmployeeId).distinct().toList());
        Map<Long, String> suppliers = supplierApi.findByIds(
                        contracts.stream().map(Contract::getSupplierId).distinct().toList()).stream()
                .collect(toMap(SupplierInfo::id, SupplierInfo::name));
        Map<Long, ProductInfo> products = productApi.findByIds(
                        contracts.stream().map(Contract::getProductId).distinct().toList()).stream()
                .collect(toMap(ProductInfo::id, p -> p));
        return new RefNames(customers, employees, suppliers, products);
    }

    private Map<Long, String> loadEmployeeNames(List<Long> employeeIds) {
        if (employeeIds.isEmpty()) {
            return Map.of();
        }
        return employeeApi.findByIds(employeeIds).stream()
                .collect(toMap(EmployeeInfo::id, EmployeeInfo::name));
    }

    private record RefNames(Map<Long, String> customer,
                            Map<Long, String> employee,
                            Map<Long, String> supplier,
                            Map<Long, ProductInfo> product) {
    }
}
