package io.github.ladium1.erp.contract.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    private static final String TEST_LOGIN_ID = "sales01";

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
    @Mock private SupplierApi supplierApi;
    @Mock private ProductApi productApi;
    @Mock private DataScopeResolver dataScopeResolver;
    @Mock private DataScopeContextProvider dataScopeContextProvider;

    @BeforeEach
    void setupVisibility() {
        // 모든 테스트 default — ALL 스코프로 통과 (행 가시성 제한 없음).
        // 가시성 자체를 검증하는 테스트가 stub 을 덮어쓴다.
        lenient().when(dataScopeResolver.resolve(Menu.CONTRACTS)).thenReturn(DataScope.ALL);
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
        given(employeeApi.getById(2L)).willReturn(employeeInfo(2L, "김영업"));
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
    }

    @Test
    @DisplayName("create 실패 — 중복 계약 번호 시 DUPLICATE_CONTRACT_NO")
    void create_fail_duplicate_contract_no() {
        // given
        ContractCreateRequest request = baseCreateRequest(null);
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.getById(2L)).willReturn(employeeInfo(2L, "김영업"));
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
        given(employeeApi.getById(2L)).willReturn(employeeInfo(2L, "김영업"));
        given(productApi.getById(3L)).willReturn(productInfo());
        given(codeRuleApi.getRule(CodeRuleTarget.CONTRACT)).willReturn(ruleWithMode(InputMode.MANUAL));

        // when & then
        assertThatThrownBy(() -> contractService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NO_REQUIRED);
    }

    @Test
    @DisplayName("update 성공 — 필드 반영 + 공급사 스냅샷 재파생")
    void update_success() {
        // given
        Contract contract = mockContract(2L);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(employeeApi.getById(9L)).willReturn(employeeInfo(9L, "박이사"));
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
    @DisplayName("update 실패 — 존재하지 않는 계약")
    void update_fail_not_found() {
        given(contractRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.update(99L, baseUpdateRequest(2L, 100_000_000L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — 대금 / 메모도 함께 삭제")
    void delete_success() {
        // given
        Contract contract = mockContract(2L);
        ReflectionTestUtils.setField(contract, "id", 1L);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        // when
        contractService.delete(1L);

        // then
        verify(paymentRepository).deleteByContractId(1L);
        verify(noteRepository).deleteByContractId(1L);
        verify(contractRepository).delete(contract);
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 계약")
    void delete_fail_not_found() {
        given(contractRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ContractErrorCode.CONTRACT_NOT_FOUND);
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

    private ContractUpdateRequest baseUpdateRequest(Long employeeId, Long finalAmount) {
        return new ContractUpdateRequest(
                1L, employeeId, 3L,
                null, null, null,
                null, finalAmount, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, null, null, null, null, null,
                null, ContractStatus.CONTRACTED
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
        return ProductInfo.builder()
                .id(3L).categoryId(1L).categoryName("평판 레이저")
                .modelName("HLA-1530").supplierId(7L).active(true)
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
