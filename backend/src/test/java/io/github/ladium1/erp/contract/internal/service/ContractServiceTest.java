package io.github.ladium1.erp.contract.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.contract.api.ContractDeletingEvent;
import io.github.ladium1.erp.contract.api.ContractInstalledEvent;
import io.github.ladium1.erp.contract.api.dto.ContractOutstandingSummary;
import io.github.ladium1.erp.contract.api.dto.MonthlyContractStat;
import io.github.ladium1.erp.contract.internal.dto.ContractCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractDetailResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractNoteCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractNoteResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractPaymentRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractPaymentResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.dto.ContractSummaryResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractUpdateRequest;
import io.github.ladium1.erp.contract.internal.entity.Contract;
import io.github.ladium1.erp.contract.internal.entity.ContractNote;
import io.github.ladium1.erp.contract.internal.entity.ContractPayment;
import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.SupportProgramStatus;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    private static final String TEST_LOGIN_ID = "sales01";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-11T03:00:00Z");
    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 8, 11);

    @InjectMocks
    private ContractService contractService;

    @Mock private ContractRepository contractRepository;
    @Mock private ContractPaymentRepository paymentRepository;
    @Mock private ContractNoteRepository noteRepository;
    @Mock private ContractMapper contractMapper;
    @Mock private ContractExcelExporter excelExporter;
    @Mock private CodeRuleApi codeRuleApi;
    @Mock private CustomerApi customerApi;
    @Mock private EmployeeApi employeeApi;
    @Mock private Clock businessClock;
    @Mock private SupplierApi supplierApi;
    @Mock private ProductApi productApi;
    @Mock private DataScopeResolver dataScopeResolver;
    @Mock private DataScopeContextProvider dataScopeContextProvider;
    @Mock private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setupVisibility() {
        // 모든 테스트 default — ALL 스코프로 통과 (행 가시성 제한 없음).
        // 가시성 자체를 검증하는 테스트가 stub 을 덮어쓴다.
        lenient().when(dataScopeResolver.resolve(Menu.CONTRACTS)).thenReturn(DataScope.ALL);
        lenient().when(businessClock.instant()).thenReturn(FIXED_INSTANT);
        lenient().when(businessClock.getZone()).thenReturn(BUSINESS_ZONE);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("search 성공 — 참조 이름 enrich + 미수금 자동 산출")
    void search_success() {
        // given
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        Pageable pageable = PageRequest.of(0, 20);
        given(contractRepository.search(any(ContractSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(contract), pageable, 1));
        stubRefNames();
        given(paymentRepository.sumPaidAmountByContractIds(List.of(1L)))
                .willReturn(Map.of(1L, 30_000_000L));

        // when
        PageResponse<ContractSummaryResponse> page =
                contractService.search(emptyCondition(), pageable);

        // then
        ContractSummaryResponse summary = page.content().getFirst();
        assertThat(summary.contractNo()).isEqualTo("CT2026-001");
        assertThat(summary.customerName()).isEqualTo("대성상사");
        assertThat(summary.employeeName()).isEqualTo("김영업");
        assertThat(summary.supplierName()).isEqualTo("YAWEI");
        assertThat(summary.productModelName()).isEqualTo("HLA-1530");
        assertThat(summary.categoryName()).isEqualTo("평판 레이저");
        assertThat(summary.outstandingAmount()).isEqualTo(70_000_000L);
    }

    @Test
    @DisplayName("search — SELF 스코프면 본인 계약자 ID 로 조건 좁힘")
    void search_scope_self() {
        // given
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current())
                .willReturn(new DataScopeContext(5L, 10L, Set.of(10L)));
        Pageable pageable = PageRequest.of(0, 20);
        given(contractRepository.search(any(ContractSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        contractService.search(emptyCondition(), pageable);

        // then
        ArgumentCaptor<ContractSearchCondition> captor = ArgumentCaptor.forClass(ContractSearchCondition.class);
        verify(contractRepository).search(captor.capture(), eq(pageable));
        assertThat(captor.getValue().employeeIdScope()).containsExactly(5L);
    }

    @Test
    @DisplayName("search — 스코프 대상 직원이 없으면 빈 페이지 (DB 미조회)")
    void search_scope_empty_returns_empty_page() {
        // given
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current()).willReturn(DataScopeContext.anonymous());

        // when
        PageResponse<ContractSummaryResponse> page =
                contractService.search(emptyCondition(), PageRequest.of(0, 20));

        // then
        assertThat(page.content()).isEmpty();
        verify(contractRepository, never()).search(any(), any());
    }

    @Test
    @DisplayName("getDetail 성공 — 대금 / 메모 포함 + 입금 합계 산출")
    void get_detail_success() {
        // given
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));
        stubRefNames();

        ContractPayment paid = payment(1L, "계약금", 30_000_000L);
        ContractPayment unpaid = payment(1L, "잔금", null);
        given(paymentRepository.findByContractIdOrderByIdAsc(1L)).willReturn(List.of(paid, unpaid));
        given(contractMapper.toPaymentResponse(any()))
                .willReturn(ContractPaymentResponse.builder().build());

        ContractNote note = ContractNote.builder().contractId(1L).authorEmployeeId(2L).content("납기 1주 연기").build();
        given(noteRepository.findByContractIdOrderByIdDesc(1L)).willReturn(List.of(note));
        given(contractMapper.toNoteResponse(note, "김영업"))
                .willReturn(ContractNoteResponse.builder().authorName("김영업").content("납기 1주 연기").build());

        // when
        ContractDetailResponse detail = contractService.getDetail(1L);

        // then
        assertThat(detail.paidTotal()).isEqualTo(30_000_000L);
        assertThat(detail.outstandingAmount()).isEqualTo(70_000_000L);
        assertThat(detail.payments()).hasSize(2);
        assertThat(detail.notes()).hasSize(1);
        assertThat(detail.notes().getFirst().authorName()).isEqualTo("김영업");
    }

    @Test
    @DisplayName("getDetail 실패 — CONTRACT_NOT_FOUND")
    void get_detail_fail_not_found() {
        given(contractRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.getDetail(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NOT_FOUND);
    }

    @Test
    @DisplayName("getDetail — SELF 스코프에서 남의 계약은 NOT_FOUND (존재 은닉)")
    void get_detail_fail_invisible() {
        // given
        Contract othersContract = mockContract(6L);
        given(contractRepository.findById(1L)).willReturn(Optional.of(othersContract));
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current())
                .willReturn(new DataScopeContext(5L, 10L, Set.of(10L)));

        // when & then
        assertThatThrownBy(() -> contractService.getDetail(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NOT_FOUND);
    }

    @Test
    @DisplayName("create 성공 — AUTO 채번 + 제품의 공급사를 스냅샷 저장")
    void create_auto_mode_success() {
        // given
        ContractCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(true);
        given(productApi.getById(3L)).willReturn(productInfo());
        given(codeRuleApi.getRule(CodeRuleTarget.CONTRACT)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.CONTRACT), any())).willReturn("CT2026-001");
        given(contractRepository.existsByContractNo("CT2026-001")).willReturn(false);

        Contract saved = mockContract(2L);
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(contractRepository.save(any(Contract.class))).willReturn(saved);

        // when
        Long id = contractService.create(request);

        // then
        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(captor.capture());
        assertThat(captor.getValue().getContractNo()).isEqualTo("CT2026-001");
        assertThat(captor.getValue().getSupplierId()).isEqualTo(7L);
        verify(codeRuleApi).generate(
                CodeRuleTarget.CONTRACT,
                CodeGenerationContext.onDate(LocalDate.of(2026, 1, 10))
        );
    }

    @Test
    @DisplayName("create — SETTLED 상태로 직접 등록해도 ContractInstalledEvent 발행")
    void create_settled_publishes_installed_event() {
        ContractCreateRequest request = settledCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(true);
        given(productApi.getById(3L)).willReturn(productInfo());
        given(codeRuleApi.getRule(CodeRuleTarget.CONTRACT)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.CONTRACT), any())).willReturn("CT2026-001");
        given(contractRepository.existsByContractNo("CT2026-001")).willReturn(false);

        Contract saved = Contract.builder()
                .contractNo("CT2026-001")
                .customerId(1L).employeeId(2L).supplierId(7L).productId(3L)
                .finalAmount(100_000_000L)
                .supportProgramStatus(SupportProgramStatus.NONE)
                .contractDate(LocalDate.of(2026, 1, 10))
                .orderDate(LocalDate.of(2026, 1, 15))
                .arrivalDate(LocalDate.of(2026, 2, 15))
                .installedDate(LocalDate.of(2026, 3, 2))
                .settledDate(LocalDate.of(2026, 3, 10))
                .status(ContractStatus.SETTLED)
                .build();
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(contractRepository.save(any(Contract.class))).willReturn(saved);

        contractService.create(request);

        ArgumentCaptor<ContractInstalledEvent> captor = ArgumentCaptor.forClass(ContractInstalledEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().contractId()).isEqualTo(100L);
        assertThat(captor.getValue().installedDate()).isEqualTo(LocalDate.of(2026, 3, 2));
    }

    @Test
    @DisplayName("create 실패 — 중복 계약 번호 시 DUPLICATE_CONTRACT_NO")
    void create_fail_duplicate_contract_no() {
        // given
        ContractCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(true);
        given(productApi.getById(3L)).willReturn(productInfo());
        given(codeRuleApi.getRule(CodeRuleTarget.CONTRACT)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.CONTRACT), any())).willReturn("CT2026-001");
        given(contractRepository.existsByContractNo("CT2026-001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.DUPLICATE_CONTRACT_NO);
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — MANUAL 모드인데 계약 번호 미입력 시 CONTRACT_NO_REQUIRED")
    void create_manual_mode_missing_contract_no() {
        // given
        ContractCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(true);
        given(productApi.getById(3L)).willReturn(productInfo());
        given(codeRuleApi.getRule(CodeRuleTarget.CONTRACT)).willReturn(ruleWithMode(InputMode.MANUAL));

        // when & then
        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NO_REQUIRED);
    }

    @Test
    @DisplayName("create 성공 — MANUAL 계약번호의 날짜 토큰을 계약일 기준으로 검증")
    void create_manual_mode_validates_contract_date_scope() {
        ContractCreateRequest request = baseCreateRequest("CT2026-009");
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(true);
        given(productApi.getById(3L)).willReturn(productInfo());
        given(codeRuleApi.getRule(CodeRuleTarget.CONTRACT)).willReturn(ruleWithMode(InputMode.MANUAL));
        given(contractRepository.existsByContractNo("CT2026-009")).willReturn(false);
        Contract saved = mockContract(2L);
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(contractRepository.save(any(Contract.class))).willReturn(saved);

        contractService.create(request);

        verify(codeRuleApi).validate(
                CodeRuleTarget.CONTRACT,
                "CT2026-009",
                CodeGenerationContext.onDate(LocalDate.of(2026, 1, 10))
        );
    }

    @Test
    @DisplayName("create 실패 — 휴직·퇴사 직원에게 신규 계약을 배정할 수 없음")
    void create_rejects_inactive_employee() {
        ContractCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(false);

        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INVALID_EMPLOYEE);
        verify(productApi, never()).getById(any());
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — SELF 범위 밖 직원을 계약 담당자로 지정할 수 없음")
    void create_rejects_employee_outside_self_scope() {
        ContractCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(true);
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current())
                .willReturn(new DataScopeContext(5L, 10L, Set.of(10L)));

        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.EMPLOYEE_OUT_OF_SCOPE);
        verify(productApi, never()).getById(any());
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — 비활성 제품 모델에는 새 계약을 연결할 수 없음")
    void create_rejects_inactive_product() {
        ContractCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(2L)).willReturn(true);
        given(productApi.getById(3L)).willReturn(productInfo(3L, false));

        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INACTIVE_PRODUCT);
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — INSTALLED 상태에는 발주·입고·설치일이 모두 필요")
    void create_rejects_installed_without_milestones() {
        ContractCreateRequest request = new ContractCreateRequest(
                null, 1L, 2L, 3L, null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, null, null, null,
                LocalDate.of(2026, 3, 2), null, null, ContractStatus.INSTALLED);

        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INVALID_DATE_FLOW);
        verify(customerApi, never()).getById(any());
    }

    @Test
    @DisplayName("create 실패 — CONTRACTED 상태에는 후속 실제 날짜를 저장할 수 없음")
    void create_rejects_status_date_mismatch() {
        ContractCreateRequest request = new ContractCreateRequest(
                null, 1L, 2L, 3L, null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, LocalDate.of(2026, 1, 11),
                null, null, null, null, null, ContractStatus.CONTRACTED);

        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INVALID_DATE_FLOW);
    }

    @Test
    @DisplayName("create 실패 — 정산일은 설치일보다 빠를 수 없음")
    void create_rejects_settlement_before_installation() {
        ContractCreateRequest request = new ContractCreateRequest(
                null, 1L, 2L, 3L, null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, LocalDate.of(2026, 1, 11),
                null, LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 30),
                LocalDate.of(2026, 1, 29), null, ContractStatus.SETTLED);

        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INVALID_DATE_FLOW);
    }

    @Test
    @DisplayName("create 실패 — 발주·입고·설치·정산 실제 날짜는 미래일 수 없음")
    void create_rejects_future_actual_milestones() {
        LocalDate tomorrow = FIXED_TODAY.plusDays(1);
        ContractCreateRequest request = new ContractCreateRequest(
                null, 1L, 2L, 3L, null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                tomorrow.minusDays(1), null, tomorrow, null, tomorrow,
                tomorrow, null, null, ContractStatus.INSTALLED);

        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INVALID_DATE_FLOW);
        verify(customerApi, never()).getById(any());
    }

    @Test
    @DisplayName("update 성공 — 필드 반영 + 공급사 스냅샷 재파생")
    void update_success() {
        // given
        Contract contract = mockContract(2L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.isEligibleForNewWorkReference(9L)).willReturn(true);
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.DEPARTMENT);
        given(dataScopeContextProvider.current())
                .willReturn(new DataScopeContext(5L, 10L, Set.of(10L)));
        given(employeeApi.findIdsByDepartmentIds(List.of(10L))).willReturn(List.of(2L, 9L));
        given(productApi.getById(3L)).willReturn(productInfo());
        ContractUpdateRequest request = baseUpdateRequest(9L, 250_000_000L);

        // when
        contractService.update(1L, request);

        // then
        assertThat(contract.getEmployeeId()).isEqualTo(9L);
        assertThat(contract.getFinalAmount()).isEqualTo(250_000_000L);
        assertThat(contract.getSupplierId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("update 실패 — 기존 계약도 비활성 직원에게 새로 배정할 수 없음")
    void update_rejects_inactive_employee() {
        Contract contract = mockContract(2L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(employeeApi.isEligibleForNewWorkReference(9L)).willReturn(false);

        assertThatThrownBy(() -> contractService.update(1L, baseUpdateRequest(9L, 100_000_000L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INVALID_EMPLOYEE);
        verify(productApi, never()).getById(any());
    }

    @Test
    @DisplayName("update 실패 — 조회 범위 밖 직원에게 계약을 재배정할 수 없음")
    void update_rejects_employee_outside_department_tree_scope() {
        Contract contract = mockContract(2L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(employeeApi.isEligibleForNewWorkReference(9L)).willReturn(true);
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.DEPARTMENT_TREE);
        given(dataScopeContextProvider.current())
                .willReturn(new DataScopeContext(5L, 10L, Set.of(10L, 11L)));
        given(employeeApi.findIdsByDepartmentIds(anyCollection())).willReturn(List.of(2L, 8L));

        assertThatThrownBy(() -> contractService.update(1L, baseUpdateRequest(9L, 100_000_000L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.EMPLOYEE_OUT_OF_SCOPE);
        verify(productApi, never()).getById(any());
    }

    @Test
    @DisplayName("update 성공 — 기존 담당자의 재직 상태가 바뀌어도 다른 필드는 수정 가능")
    void update_keeps_existing_ineligible_employee_reference() {
        Contract contract = mockContract(2L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());

        contractService.update(1L, baseUpdateRequest(2L, 250_000_000L));

        assertThat(contract.getFinalAmount()).isEqualTo(250_000_000L);
        verify(employeeApi, never()).isEligibleForNewWorkReference(any());
    }

    @Test
    @DisplayName("update 성공 — 기존 비활성 제품 모델 참조는 그대로 유지 가능")
    void update_keeps_existing_inactive_product() {
        Contract contract = mockContract(2L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo(3L, false));

        contractService.update(1L, baseUpdateRequest(2L, 3L, 250_000_000L));

        assertThat(contract.getProductId()).isEqualTo(3L);
        assertThat(contract.getFinalAmount()).isEqualTo(250_000_000L);
    }

    @Test
    @DisplayName("update 실패 — 제품 모델을 비활성 대상으로 변경할 수 없음")
    void update_rejects_new_inactive_product() {
        Contract contract = mockContract(2L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(4L)).willReturn(productInfo(4L, false));

        assertThatThrownBy(() -> contractService.update(
                1L, baseUpdateRequest(2L, 4L, 100_000_000L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.INACTIVE_PRODUCT);
        assertThat(contract.getProductId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("update 실패 — 계약번호의 날짜 scope를 벗어난 계약일 변경")
    void update_rejects_contract_date_outside_code_scope() {
        Contract contract = mockContract(2L);
        ContractUpdateRequest nextYear = new ContractUpdateRequest(
                1L, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2027, 1, 10), null, null, null, null, null, null,
                null, ContractStatus.CONTRACTED
        );
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        org.mockito.BDDMockito.willThrow(
                new BusinessException(CodeRuleErrorCode.CODE_FORMAT_MISMATCH)
        ).given(codeRuleApi).validate(
                CodeRuleTarget.CONTRACT,
                "CT2026-001",
                CodeGenerationContext.onDate(LocalDate.of(2027, 1, 10))
        );

        assertThatThrownBy(() -> contractService.update(1L, nextYear))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.CODE_FORMAT_MISMATCH);
        verify(customerApi, never()).getById(any());
    }

    @Test
    @DisplayName("update 실패 — 설비 생성 뒤에는 비설치 상태로 되돌릴 수 없음")
    void update_rejects_installed_status_rollback() {
        Contract contract = installedContract();
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());

        assertThatThrownBy(() -> contractService.update(1L, baseUpdateRequest(2L, 100_000_000L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", ContractErrorCode.INSTALLED_CONTRACT_SNAPSHOT_IMMUTABLE);
    }

    @Test
    @DisplayName("update 실패 — 설비 생성 뒤 고객사·제품·출력·설치일 스냅샷은 변경 불가")
    void update_rejects_installed_snapshot_change() {
        Contract contract = installedContract();
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(2L)).willReturn(CustomerInfo.builder().id(2L).build());
        given(productApi.getById(3L)).willReturn(productInfo());
        ContractUpdateRequest changedCustomer = new ContractUpdateRequest(
                2L, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null,
                LocalDate.of(2026, 1, 15), null, LocalDate.of(2026, 2, 15),
                LocalDate.of(2026, 3, 2), null,
                null, ContractStatus.INSTALLED);

        assertThatThrownBy(() -> contractService.update(1L, changedCustomer))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", ContractErrorCode.INSTALLED_CONTRACT_SNAPSHOT_IMMUTABLE);
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 계약")
    void update_fail_not_found() {
        given(contractRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.update(99L, baseUpdateRequest(2L, 100_000_000L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NOT_FOUND);
    }

    @Test
    @DisplayName("update — INSTALLED 로 전이 시 ContractInstalledEvent 발행")
    void update_transition_to_installed_publishes_event() {
        // given
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());

        // when
        contractService.update(1L, installedUpdateRequest());

        // then
        ArgumentCaptor<ContractInstalledEvent> captor = ArgumentCaptor.forClass(ContractInstalledEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().contractId()).isEqualTo(1L);
        assertThat(captor.getValue().customerId()).isEqualTo(1L);
        assertThat(captor.getValue().supplierId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("update — 비설치 상태에서 SETTLED 로 바로 전이해도 ContractInstalledEvent 발행")
    void update_transition_to_settled_publishes_event() {
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());

        contractService.update(1L, settledUpdateRequest());

        verify(eventPublisher).publishEvent(any(ContractInstalledEvent.class));
    }

    @Test
    @DisplayName("update — 이미 INSTALLED 인 계약 재저장은 이벤트 미발행")
    void update_already_installed_no_event() {
        // given
        Contract contract = Contract.builder()
                .contractNo("CT2026-001")
                .customerId(1L).employeeId(2L).supplierId(7L).productId(3L)
                .finalAmount(100_000_000L)
                .supportProgramStatus(SupportProgramStatus.NONE)
                .contractDate(LocalDate.of(2026, 1, 10))
                .orderDate(LocalDate.of(2026, 1, 15))
                .arrivalDate(LocalDate.of(2026, 2, 15))
                .installedDate(LocalDate.of(2026, 3, 2))
                .status(ContractStatus.INSTALLED)
                .build();
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());

        // when
        contractService.update(1L, installedUpdateRequest());

        // then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("update — INSTALLED 에서 SETTLED 로 전이할 때 설비 이벤트 중복 발행 안 함")
    void update_installed_to_settled_no_duplicate_event() {
        Contract contract = Contract.builder()
                .contractNo("CT2026-001")
                .customerId(1L).employeeId(2L).supplierId(7L).productId(3L)
                .finalAmount(100_000_000L)
                .supportProgramStatus(SupportProgramStatus.NONE)
                .contractDate(LocalDate.of(2026, 1, 10))
                .orderDate(LocalDate.of(2026, 1, 15))
                .arrivalDate(LocalDate.of(2026, 2, 15))
                .installedDate(LocalDate.of(2026, 3, 2))
                .status(ContractStatus.INSTALLED)
                .build();
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());

        contractService.update(1L, settledUpdateRequest());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("update — 설치 뒤 제품 마스터 공급사가 바뀌어도 계약 공급사 스냅샷 보존")
    void update_installed_preserves_supplier_snapshot() {
        Contract contract = installedContract();
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(ProductInfo.builder()
                .id(3L).supplierId(99L).active(true).build());

        contractService.update(1L, settledUpdateRequest());

        assertThat(contract.getSupplierId()).isEqualTo(7L);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("delete 성공 — ContractDeletingEvent 발행 + 대금 / 메모도 함께 삭제")
    void delete_success() {
        // given
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));

        // when
        contractService.delete(1L);

        // then
        verify(eventPublisher).publishEvent(new ContractDeletingEvent(1L));
        verify(paymentRepository).deleteByContractId(1L);
        verify(noteRepository).deleteByContractId(1L);
        verify(contractRepository).delete(contract);
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 계약")
    void delete_fail_not_found() {
        given(contractRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NOT_FOUND);
        verify(contractRepository, never()).delete(any(Contract.class));
    }

    @Test
    @DisplayName("delete 실패 — 설비 생성 대상이 된 설치·정산 계약은 비동기 처리 중에도 삭제 금지")
    void delete_rejects_installed_contract() {
        Contract contract = installedContract();
        given(contractRepository.findByIdForUpdate(1L)).willReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", ContractErrorCode.INSTALLED_CONTRACT_DELETE_FORBIDDEN);
        verify(eventPublisher, never()).publishEvent(any());
        verify(contractRepository, never()).delete(any(Contract.class));
    }

    @Test
    @DisplayName("createPayment 성공 — 라벨 trim 후 저장")
    void create_payment_success() {
        // given
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));
        ContractPayment saved = payment(1L, "계약금", 30_000_000L);
        ReflectionTestUtils.setField(saved, "id", 10L);
        given(paymentRepository.save(any(ContractPayment.class))).willReturn(saved);

        // when
        Long id = contractService.createPayment(1L, paymentRequest(" 계약금 "));

        // then
        assertThat(id).isEqualTo(10L);
        ArgumentCaptor<ContractPayment> captor = ArgumentCaptor.forClass(ContractPayment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getLabel()).isEqualTo("계약금");
    }

    @Test
    @DisplayName("updatePayment 실패 — PAYMENT_NOT_FOUND")
    void update_payment_fail_not_found() {
        given(paymentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.updatePayment(99L, paymentRequest("계약금")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("deletePayment 성공")
    void delete_payment_success() {
        // given
        ContractPayment paymentRow = payment(1L, "계약금", null);
        given(paymentRepository.findById(10L)).willReturn(Optional.of(paymentRow));
        given(contractRepository.findById(1L)).willReturn(Optional.of(mockContract(2L)));

        // when
        contractService.deletePayment(10L);

        // then
        verify(paymentRepository).delete(paymentRow);
    }

    @Test
    @DisplayName("createNote 성공 — 현재 로그인 직원을 작성자로 저장")
    void create_note_success() {
        // given
        authenticate();
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));
        given(employeeApi.findByLoginId(TEST_LOGIN_ID))
                .willReturn(Optional.of(employeeInfo(5L, "이과장")));
        ContractNote saved = ContractNote.builder().contractId(1L).authorEmployeeId(5L).content("메모").build();
        ReflectionTestUtils.setField(saved, "id", 20L);
        given(noteRepository.save(any(ContractNote.class))).willReturn(saved);

        // when
        Long id = contractService.createNote(1L, new ContractNoteCreateRequest("납기 1주 연기 협의"));

        // then
        assertThat(id).isEqualTo(20L);
        ArgumentCaptor<ContractNote> captor = ArgumentCaptor.forClass(ContractNote.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getAuthorEmployeeId()).isEqualTo(5L);
        assertThat(captor.getValue().getContent()).isEqualTo("납기 1주 연기 협의");
    }

    @Test
    @DisplayName("createNote 실패 — 작성자 미확인 시 AUTHOR_NOT_RESOLVED")
    void create_note_fail_author_not_resolved() {
        // given
        authenticate();
        given(contractRepository.findById(1L)).willReturn(Optional.of(mockContract(2L)));
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contractService.createNote(1L, new ContractNoteCreateRequest("메모")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.AUTHOR_NOT_RESOLVED);
        verify(noteRepository, never()).save(any());
    }

    @Test
    @DisplayName("monthlyStats — 데이터 없는 달을 0 으로 채워 요청 개월 수만큼 반환")
    void monthly_stats_fills_empty_months() {
        // given
        java.time.YearMonth current = java.time.YearMonth.now();
        given(contractRepository.monthlyStats(any(LocalDate.class), eq(null)))
                .willReturn(List.of(MonthlyContractStat.builder()
                        .month(current.toString()).count(2).totalAmount(300_000_000L).build()));

        // when
        List<MonthlyContractStat> stats = contractService.monthlyStats(3);

        // then
        assertThat(stats).hasSize(3);
        assertThat(stats.getFirst().month()).isEqualTo(current.minusMonths(2).toString());
        assertThat(stats.getFirst().count()).isZero();
        assertThat(stats.getLast().month()).isEqualTo(current.toString());
        assertThat(stats.getLast().totalAmount()).isEqualTo(300_000_000L);
    }

    @Test
    @DisplayName("monthlyStats — 스코프 대상 직원이 없으면 전부 0 (DB 미조회)")
    void monthly_stats_scope_empty() {
        // given
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current()).willReturn(DataScopeContext.anonymous());

        // when
        List<MonthlyContractStat> stats = contractService.monthlyStats(3);

        // then
        assertThat(stats).hasSize(3);
        assertThat(stats).allMatch(s -> s.count() == 0 && s.totalAmount() == 0);
        verify(contractRepository, never()).monthlyStats(any(), any());
    }

    @Test
    @DisplayName("outstandingSummary — 스코프 대상 직원이 없으면 0 요약 (DB 미조회)")
    void outstanding_summary_scope_empty() {
        // given
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current()).willReturn(DataScopeContext.anonymous());

        // when
        ContractOutstandingSummary summary = contractService.outstandingSummary();

        // then
        assertThat(summary.totalFinalAmount()).isZero();
        assertThat(summary.totalOutstandingAmount()).isZero();
        verify(contractRepository, never()).outstandingSummary(any());
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TEST_LOGIN_ID, null, List.of()));
    }

    private void stubRefNames() {
        given(customerApi.findByIds(anyList())).willReturn(List.of(customerInfo()));
        given(employeeApi.findByIds(anyList())).willReturn(List.of(employeeInfo(2L, "김영업")));
        given(supplierApi.findByIds(anyList())).willReturn(List.of(supplierInfo()));
        given(productApi.findByIds(anyList())).willReturn(List.of(productInfo()));
    }

    private Contract mockContract(Long employeeId) {
        return Contract.builder()
                .contractNo("CT2026-001")
                .customerId(1L)
                .employeeId(employeeId)
                .supplierId(7L)
                .productId(3L)
                .finalAmount(100_000_000L)
                .supportProgramStatus(SupportProgramStatus.NONE)
                .contractDate(LocalDate.of(2026, 1, 10))
                .status(ContractStatus.CONTRACTED)
                .build();
    }

    private Contract installedContract() {
        return Contract.builder()
                .contractNo("CT2026-001")
                .customerId(1L).employeeId(2L).supplierId(7L).productId(3L)
                .finalAmount(100_000_000L)
                .supportProgramStatus(SupportProgramStatus.NONE)
                .contractDate(LocalDate.of(2026, 1, 10))
                .orderDate(LocalDate.of(2026, 1, 15))
                .arrivalDate(LocalDate.of(2026, 2, 15))
                .installedDate(LocalDate.of(2026, 3, 2))
                .status(ContractStatus.INSTALLED)
                .build();
    }

    private ContractPayment payment(Long contractId, String label, Long paidAmount) {
        return ContractPayment.builder()
                .contractId(contractId)
                .label(label)
                .paidAmount(paidAmount)
                .build();
    }

    private ContractCreateRequest baseCreateRequest(String contractNo) {
        return new ContractCreateRequest(
                contractNo, 1L, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, null, null, null, null, null,
                null, ContractStatus.CONTRACTED
        );
    }

    private ContractCreateRequest settledCreateRequest(String contractNo) {
        return new ContractCreateRequest(
                contractNo, 1L, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null,
                LocalDate.of(2026, 1, 15), null, LocalDate.of(2026, 2, 15),
                LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 10),
                null, ContractStatus.SETTLED
        );
    }

    private ContractUpdateRequest baseUpdateRequest(Long employeeId, Long finalAmount) {
        return baseUpdateRequest(employeeId, 3L, finalAmount);
    }

    private ContractUpdateRequest baseUpdateRequest(Long employeeId, Long productId, Long finalAmount) {
        return new ContractUpdateRequest(
                1L, employeeId, productId,
                null, null, null,
                null, finalAmount, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, null, null, null, null, null,
                null, ContractStatus.CONTRACTED
        );
    }

    private ContractUpdateRequest installedUpdateRequest() {
        return new ContractUpdateRequest(
                1L, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null,
                LocalDate.of(2026, 1, 15), null, LocalDate.of(2026, 2, 15),
                LocalDate.of(2026, 3, 2), null,
                null, ContractStatus.INSTALLED
        );
    }

    private ContractUpdateRequest settledUpdateRequest() {
        return new ContractUpdateRequest(
                1L, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null,
                LocalDate.of(2026, 1, 15), null, LocalDate.of(2026, 2, 15),
                LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 10),
                null, ContractStatus.SETTLED
        );
    }

    private ContractPaymentRequest paymentRequest(String label) {
        return new ContractPaymentRequest(label, null, null, null, null, null, null, null);
    }

    private ContractSearchCondition emptyCondition() {
        return new ContractSearchCondition(null, null, null, null, null, null, null);
    }

    private CustomerInfo customerInfo() {
        return CustomerInfo.builder().id(1L).code("C0001").name("대성상사").build();
    }

    private EmployeeInfo employeeInfo(Long id, String name) {
        return EmployeeInfo.builder().id(id).name(name).build();
    }

    private SupplierInfo supplierInfo() {
        return SupplierInfo.builder().id(7L).name("YAWEI").active(true).build();
    }

    private ProductInfo productInfo() {
        return productInfo(3L, true);
    }

    private ProductInfo productInfo(Long id, boolean active) {
        return ProductInfo.builder()
                .id(id).categoryId(1L).categoryName("평판 레이저")
                .modelName("HLA-1530").supplierId(7L).active(active)
                .build();
    }

    private CodeRuleInfo ruleWithMode(InputMode mode) {
        return CodeRuleInfo.builder()
                .target(CodeRuleTarget.CONTRACT)
                .pattern("CT{YYYY}-{SEQ:3}")
                .inputMode(mode)
                .description("테스트")
                .build();
    }
}
