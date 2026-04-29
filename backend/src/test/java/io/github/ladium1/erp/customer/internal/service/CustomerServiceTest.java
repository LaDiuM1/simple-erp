package io.github.ladium1.erp.customer.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerDetailResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerUpdateRequest;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import io.github.ladium1.erp.customer.internal.excel.CustomerExcelExporter;
import io.github.ladium1.erp.customer.internal.exception.CustomerErrorCode;
import io.github.ladium1.erp.customer.internal.mapper.CustomerMapper;
import io.github.ladium1.erp.customer.internal.repository.CustomerRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
    @DisplayName("delete 성공")
    void delete_success() {
        given(customerRepository.existsById(1L)).willReturn(true);

        customerService.delete(1L);

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
