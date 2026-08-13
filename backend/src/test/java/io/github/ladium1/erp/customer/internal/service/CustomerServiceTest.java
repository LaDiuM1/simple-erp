package io.github.ladium1.erp.customer.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.customer.api.CustomerDeletingEvent;
import io.github.ladium1.erp.customer.api.CustomerVisibilityContributor;
import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerDetailResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerReferenceResponse;
import io.github.ladium1.erp.customer.internal.dto.SalesCustomerReferenceResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerSearchCondition;
import io.github.ladium1.erp.customer.internal.dto.CustomerSummaryResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerUpdateRequest;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import io.github.ladium1.erp.customer.internal.excel.CustomerExcelExporter;
import io.github.ladium1.erp.customer.internal.exception.CustomerErrorCode;
import io.github.ladium1.erp.customer.internal.mapper.CustomerMapper;
import io.github.ladium1.erp.customer.internal.repository.CustomerRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.demo.DemoExcelExportGuard;
import io.github.ladium1.erp.global.demo.DemoErrorCode;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.security.DataScopeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerMapper customerMapper;
    @Mock private CodeRuleApi codeRuleApi;
    @Mock private CustomerExcelExporter customerExcelExporter;
    @Mock private DataScopeResolver dataScopeResolver;
    @Mock private DataScopeContextProvider dataScopeContextProvider;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private DemoExcelExportGuard demoExcelExportGuard;

    @Test
    @DisplayName("Excel preflight 실패는 고객 전체 목록 materialization 전에 중단")
    void excel_preflight_runs_before_search_all() {
        org.mockito.BDDMockito.willThrow(new BusinessException(DemoErrorCode.DEMO_EXCEL_EXPORT_TOO_LARGE))
                .given(demoExcelExportGuard)
                .assertExportAllowed(DemoExcelExportGuard.Table.CUSTOMERS);

        assertThatThrownBy(() -> customerService.exportExcel(null, org.springframework.data.domain.Sort.unsorted()))
                .isInstanceOf(BusinessException.class);

        verify(customerRepository, never()).searchAll(any(), any());
    }
    @Spy private List<CustomerVisibilityContributor> visibilityContributors = new ArrayList<>();

    @BeforeEach
    void setupVisibility() {
        // 모든 테스트 default — ALL 스코프로 통과 (행 가시성 제한 없음).
        // 가시성 자체를 검증하는 테스트가 추가되면 그 테스트가 stub 을 덮어쓴다.
        lenient().when(dataScopeResolver.resolveMostPermissive(any(Menu[].class)))
                .thenReturn(DataScope.ALL);
    }

    @Test
    @DisplayName("getDetail 성공 — Mapper 가 변환한 Detail 반환")
    void get_detail_success() {
        // given
        Customer customer = mockCustomer("C0001", "대성상사");
        CustomerDetailResponse detail = CustomerDetailResponse.builder()
                .id(1L).code("C0001").name("대성상사").build();
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(customerMapper.toDetailResponse(customer)).willReturn(detail);

        // when
        CustomerDetailResponse actual = customerService.getDetail(1L);

        // then
        assertThat(actual).isEqualTo(detail);
    }

    @Test
    @DisplayName("getDetail 실패 — CUSTOMER_NOT_FOUND")
    void get_detail_fail_not_found() {
        given(customerRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getDetail(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Test
    @DisplayName("고객사 관리 목록은 다른 메뉴의 데이터 범위와 결합하지 않는다")
    void management_search_does_not_combine_other_menu_scope() {
        CustomerSearchCondition condition = new CustomerSearchCondition(null, null, null, null, null, null);
        PageRequest pageable = PageRequest.of(0, 20);
        Customer customer = mockCustomer("C0001", "대성상사");
        CustomerSummaryResponse summary = CustomerSummaryResponse.builder()
                .id(1L).code("C0001").name("대성상사").build();
        given(customerRepository.search(condition, pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(customerMapper.toSummaryResponse(customer)).willReturn(summary);

        assertThat(customerService.search(condition, pageable).content())
                .containsExactly(summary);
        verify(dataScopeResolver, never()).resolveMostPermissive(any(Menu[].class));
    }

    @Test
    @DisplayName("영업 고객 참조 목록은 영업 메뉴의 데이터 범위를 적용한다")
    void sales_reference_search_uses_sales_customer_scope() {
        CustomerSearchCondition condition = new CustomerSearchCondition(null, null, null, null, null, null);
        PageRequest pageable = PageRequest.of(0, 20);
        Customer customer = mockCustomer("C0001", "대성상사");
        SalesCustomerReferenceResponse reference = SalesCustomerReferenceResponse.builder()
                .id(1L).code("C0001").name("대성상사").build();
        DataScopeContext context = new DataScopeContext(10L, 20L, Set.of(20L));
        given(dataScopeResolver.resolveMostPermissive(Menu.SALES_CUSTOMERS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current()).willReturn(context);
        visibilityContributors.add((scope, actualContext) -> Set.of(1L));
        CustomerSearchCondition scoped = condition.withIdScope(Set.of(1L));
        given(customerRepository.search(scoped, pageable)).willReturn(new PageImpl<>(List.of(customer)));
        given(customerMapper.toSalesReferenceResponse(customer)).willReturn(reference);

        assertThat(customerService.searchSalesReference(condition, pageable).content())
                .containsExactly(reference);
        verify(dataScopeResolver).resolveMostPermissive(Menu.SALES_CUSTOMERS);
    }

    @Test
    @DisplayName("고객 자체 조회는 통합 메뉴 범위를 적용해 숨은 고객을 NOT_FOUND로 은닉")
    void current_viewer_visibility_boundary() {
        DataScopeContext context = new DataScopeContext(10L, 20L, Set.of(20L));
        given(dataScopeResolver.resolveMostPermissive(Menu.SALES_CUSTOMERS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current()).willReturn(context);
        visibilityContributors.add((scope, actualContext) -> Set.of(1L, 3L));

        assertThatThrownBy(() -> customerService.getSalesReference(2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CUSTOMER_NOT_FOUND);
        verify(customerRepository, never()).findById(2L);
    }

    @Test
    @DisplayName("메뉴별 가시성 경계 — CUSTOMERS ALL이 SALES_CUSTOMERS SELF를 넓히지 않음")
    void menu_specific_visibility_does_not_use_combined_scope() {
        DataScopeContext context = new DataScopeContext(10L, 20L, Set.of(20L));
        given(dataScopeResolver.resolveMostPermissive(Menu.SALES_CUSTOMERS))
                .willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current()).willReturn(context);
        visibilityContributors.add((scope, actualContext) -> Set.of(1L, 3L));

        assertThat(customerService.currentViewerIdRestriction(Menu.SALES_CUSTOMERS))
                .contains(Set.of(1L, 3L));
        assertThat(customerService.filterVisibleIdsForCurrentViewer(
                Menu.SALES_CUSTOMERS,
                List.of(3L, 2L, 1L)
        )).containsExactly(3L, 1L);
        assertThatThrownBy(() -> customerService.assertVisibleToCurrentViewer(
                Menu.SALES_CUSTOMERS,
                2L
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CUSTOMER_NOT_FOUND);
        verify(dataScopeResolver, org.mockito.Mockito.atLeastOnce())
                .resolveMostPermissive(Menu.SALES_CUSTOMERS);
    }

    @Test
    @DisplayName("대시보드 고객 KPI와 최근 고객은 현재 조회자의 허용 ID만 조회")
    void dashboard_customer_reads_use_current_viewer_scope() {
        given(customerRepository.count()).willReturn(2L);
        given(customerRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .willReturn(new org.springframework.data.domain.PageImpl<>(
                        List.of(mockCustomer("C0001", "보이는 고객"))
                ));

        assertThat(customerService.countVisibleToCurrentViewer()).isEqualTo(2L);
        assertThat(customerService.findRecentVisibleToCurrentViewer(5))
                .extracting("code")
                .containsExactly("C0001");
        verify(customerRepository).count();
        verify(customerRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("isCodeAvailable — 미사용 코드면 true")
    void is_code_available_true() {
        given(customerRepository.existsByCode("C9999")).willReturn(false);
        assertThat(customerService.isCodeAvailable("C9999")).isTrue();
    }

    @Test
    @DisplayName("isCodeAvailable — 사용 중 코드면 false")
    void is_code_available_false() {
        given(customerRepository.existsByCode("C0001")).willReturn(true);
        assertThat(customerService.isCodeAvailable("C0001")).isFalse();
    }

    @Test
    @DisplayName("isCodeAvailable — 빈/공백 입력은 false (DB 미조회)")
    void is_code_available_blank_returns_false() {
        assertThat(customerService.isCodeAvailable("")).isFalse();
        assertThat(customerService.isCodeAvailable("   ")).isFalse();
        assertThat(customerService.isCodeAvailable(null)).isFalse();
        verify(customerRepository, never()).existsByCode(any());
    }

    @Test
    @DisplayName("isBizRegNoAvailable — 미사용 번호면 true")
    void is_biz_reg_no_available_true() {
        given(customerRepository.existsByBizRegNo("999-99-99999")).willReturn(false);
        assertThat(customerService.isBizRegNoAvailable("999-99-99999")).isTrue();
    }

    @Test
    @DisplayName("isBizRegNoAvailable — 빈 입력은 false (DB 미조회)")
    void is_biz_reg_no_available_blank() {
        assertThat(customerService.isBizRegNoAvailable("")).isFalse();
        assertThat(customerService.isBizRegNoAvailable(null)).isFalse();
        verify(customerRepository, never()).existsByBizRegNo(any());
    }

    @Test
    @DisplayName("create — AUTO 모드에서 시스템이 채번한 코드로 저장")
    void create_auto_mode_success() {
        // given
        CustomerCreateRequest request = baseCreateRequest(null, "신규고객사", null);
        given(codeRuleApi.getRule(CodeRuleTarget.CUSTOMER)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.CUSTOMER), any())).willReturn("C0010");
        given(customerRepository.existsByCode("C0010")).willReturn(false);
        Customer saved = mockCustomer("C0010", "신규고객사");
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(customerRepository.save(any(Customer.class))).willReturn(saved);

        // when
        Long id = customerService.create(request);

        // then
        assertThat(id).isEqualTo(100L);
    }

    @Test
    @DisplayName("create — MANUAL 모드에서 사용자 입력 코드 검증 후 저장")
    void create_manual_mode_success() {
        // given
        CustomerCreateRequest request = baseCreateRequest("CUSTOM", "직접입력", null);
        given(codeRuleApi.getRule(CodeRuleTarget.CUSTOMER)).willReturn(ruleWithMode(InputMode.MANUAL));
        given(customerRepository.existsByCode("CUSTOM")).willReturn(false);
        Customer saved = mockCustomer("CUSTOM", "직접입력");
        ReflectionTestUtils.setField(saved, "id", 5L);
        given(customerRepository.save(any(Customer.class))).willReturn(saved);

        // when
        Long id = customerService.create(request);

        // then
        assertThat(id).isEqualTo(5L);
        verify(codeRuleApi).validate(CodeRuleTarget.CUSTOMER, "CUSTOM");
        verify(codeRuleApi, never()).generate(any(), any());
    }

    @Test
    @DisplayName("create 실패 — MANUAL 모드인데 코드 미입력 시 CODE_REQUIRED")
    void create_manual_mode_missing_code() {
        // given
        CustomerCreateRequest request = baseCreateRequest(null, "이름", null);
        given(codeRuleApi.getRule(CodeRuleTarget.CUSTOMER)).willReturn(ruleWithMode(InputMode.MANUAL));

        // when & then
        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CODE_REQUIRED);
    }

    @Test
    @DisplayName("create 실패 — 중복 코드 시 DUPLICATE_CODE")
    void create_fail_duplicate_code() {
        // given
        CustomerCreateRequest request = baseCreateRequest(null, "신규", null);
        given(codeRuleApi.getRule(CodeRuleTarget.CUSTOMER)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.CUSTOMER), any())).willReturn("C0001");
        given(customerRepository.existsByCode("C0001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.DUPLICATE_CODE);
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — 중복 사업자번호 시 DUPLICATE_BIZ_REG_NO")
    void create_fail_duplicate_biz_reg_no() {
        // given
        CustomerCreateRequest request = baseCreateRequest(null, "신규", "123-45-67890");
        given(codeRuleApi.getRule(CodeRuleTarget.CUSTOMER)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.CUSTOMER), any())).willReturn("C0010");
        given(customerRepository.existsByCode("C0010")).willReturn(false);
        given(customerRepository.existsByBizRegNo("123-45-67890")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.DUPLICATE_BIZ_REG_NO);
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("update 성공 — 엔티티의 update 호출")
    void update_success() {
        // given
        Customer customer = mockCustomer("C0001", "대성상사");
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        CustomerUpdateRequest request = baseUpdateRequest("대성상사 변경", null);

        // when
        customerService.update(1L, request);

        // then
        assertThat(customer.getName()).isEqualTo("대성상사 변경");
    }

    @Test
    @DisplayName("update — 사업자번호 변경 없으면 중복 검사 생략")
    void update_skip_biz_reg_no_check_when_unchanged() {
        // given
        Customer customer = mockCustomer("C0001", "대성상사", "123-45-67890");
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        CustomerUpdateRequest request = baseUpdateRequest("대성상사", "123-45-67890");

        // when
        customerService.update(1L, request);

        // then
        verify(customerRepository, never()).existsByBizRegNo(any());
    }

    @Test
    @DisplayName("update 실패 — 다른 고객사가 쓰는 사업자번호로 변경 시 DUPLICATE_BIZ_REG_NO")
    void update_fail_duplicate_biz_reg_no() {
        // given
        Customer customer = mockCustomer("C0001", "대성상사", "111-11-11111");
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(customerRepository.existsByBizRegNo("222-22-22222")).willReturn(true);
        CustomerUpdateRequest request = baseUpdateRequest("이름", "222-22-22222");

        // when & then
        assertThatThrownBy(() -> customerService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.DUPLICATE_BIZ_REG_NO);
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 고객사")
    void update_fail_not_found() {
        // given
        given(customerRepository.findById(99L)).willReturn(Optional.empty());
        CustomerUpdateRequest request = baseUpdateRequest("이름", null);

        // when & then
        assertThatThrownBy(() -> customerService.update(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — 삭제 전 CustomerDeletingEvent 발행")
    void delete_success() {
        given(customerRepository.existsById(1L)).willReturn(true);

        customerService.delete(1L);

        verify(eventPublisher).publishEvent(new CustomerDeletingEvent(1L));
        verify(customerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 고객사")
    void delete_fail_not_found() {
        given(customerRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CUSTOMER_NOT_FOUND);
        verify(customerRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private Customer mockCustomer(String code, String name) {
        return mockCustomer(code, name, null);
    }

    private Customer mockCustomer(String code, String name, String bizRegNo) {
        return Customer.builder()
                .code(code)
                .name(name)
                .bizRegNo(bizRegNo)
                .type(CustomerType.GENERAL)
                .status(CustomerStatus.ACTIVE)
                .build();
    }

    private CustomerCreateRequest baseCreateRequest(String code, String name, String bizRegNo) {
        return new CustomerCreateRequest(
                code, name, null, bizRegNo, null, null,
                null, null, null, null, null, null,
                null, null, null,
                CustomerType.GENERAL, CustomerStatus.ACTIVE, null, null
        );
    }

    private CustomerUpdateRequest baseUpdateRequest(String name, String bizRegNo) {
        return new CustomerUpdateRequest(
                name, null, bizRegNo, null, null,
                null, null, null, null, null, null,
                null, null, null,
                CustomerType.GENERAL, CustomerStatus.ACTIVE, null, null
        );
    }

    private CodeRuleInfo ruleWithMode(InputMode mode) {
        return CodeRuleInfo.builder()
                .target(CodeRuleTarget.CUSTOMER)
                .pattern("C{SEQ:4}")
                .inputMode(mode)
                .description("테스트")
                .build();
    }
}
