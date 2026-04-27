package io.github.ladium1.erp.customer.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerDetailResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerSearchCondition;
import io.github.ladium1.erp.customer.internal.dto.CustomerSummaryResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerUpdateRequest;
import io.github.ladium1.erp.customer.internal.entity.Address;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import io.github.ladium1.erp.customer.internal.excel.CustomerExcelExporter;
import io.github.ladium1.erp.customer.internal.exception.CustomerErrorCode;
import io.github.ladium1.erp.customer.internal.mapper.CustomerMapper;
import io.github.ladium1.erp.customer.internal.repository.CustomerRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements CustomerApi {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CodeRuleApi codeRuleApi;
    private final CustomerExcelExporter customerExcelExporter;

    @Override
    public CustomerInfo getById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toCustomerInfo)
                .orElseThrow(() -> new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
    }

    @Override
    public List<CustomerInfo> findAll() {
        return customerMapper.toCustomerInfos(
                customerRepository.findAll(Sort.by("name").ascending())
        );
    }

    @Override
    public List<CustomerInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return customerMapper.toCustomerInfos(customerRepository.findAllById(ids));
    }

    public PageResponse<CustomerSummaryResponse> search(CustomerSearchCondition condition, Pageable pageable) {
        Page<Customer> page = customerRepository.search(condition, pageable);
        return PageResponse.of(page.map(customerMapper::toSummaryResponse));
    }

    public byte[] exportExcel(CustomerSearchCondition condition, Sort sort) {
        List<Customer> customers = customerRepository.searchAll(condition, sort);
        List<CustomerSummaryResponse> rows = customers.stream()
                .map(customerMapper::toSummaryResponse)
                .toList();
        return customerExcelExporter.export(rows);
    }

    public CustomerDetailResponse getDetail(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDetailResponse)
                .orElseThrow(() -> new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
    }

    /**
     * 사용자 입력 코드의 사용 가능 여부 — 등록 화면의 디바운스 중복 검사 용.
     * 빈 문자열은 false 반환.
     */
    public boolean isCodeAvailable(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return !customerRepository.existsByCode(code.trim());
    }

    /**
     * 사업자등록번호 사용 가능 여부 — 디바운스 중복 검사 용.
     */
    public boolean isBizRegNoAvailable(String bizRegNo) {
        if (bizRegNo == null || bizRegNo.isBlank()) {
            return false;
        }
        return !customerRepository.existsByBizRegNo(bizRegNo.trim());
    }

    @Transactional
    public Long create(CustomerCreateRequest request) {
        String code = resolveCode(request.code());

        if (customerRepository.existsByCode(code)) {
            throw new BusinessException(CustomerErrorCode.DUPLICATE_CODE);
        }
        String bizRegNo = trimToNull(request.bizRegNo());
        if (bizRegNo != null && customerRepository.existsByBizRegNo(bizRegNo)) {
            throw new BusinessException(CustomerErrorCode.DUPLICATE_BIZ_REG_NO);
        }

        Customer customer = Customer.builder()
                .code(code)
                .name(request.name())
                .nameEn(request.nameEn())
                .bizRegNo(bizRegNo)
                .corpRegNo(request.corpRegNo())
                .representative(request.representative())
                .bizType(request.bizType())
                .bizItem(request.bizItem())
                .phone(request.phone())
                .fax(request.fax())
                .email(request.email())
                .website(request.website())
                .address(toAddress(request.zipCode(), request.roadAddress(), request.detailAddress()))
                .type(request.type())
                .status(request.status())
                .tradeStartDate(request.tradeStartDate())
                .note(request.note())
                .build();

        return customerRepository.save(customer).getId();
    }

    @Transactional
    public void update(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND));

        String bizRegNo = trimToNull(request.bizRegNo());
        if (bizRegNo != null
                && !bizRegNo.equals(customer.getBizRegNo())
                && customerRepository.existsByBizRegNo(bizRegNo)) {
            throw new BusinessException(CustomerErrorCode.DUPLICATE_BIZ_REG_NO);
        }

        customer.update(
                request.name(),
                request.nameEn(),
                bizRegNo,
                request.corpRegNo(),
                request.representative(),
                request.bizType(),
                request.bizItem(),
                request.phone(),
                request.fax(),
                request.email(),
                request.website(),
                toAddress(request.zipCode(), request.roadAddress(), request.detailAddress()),
                request.type(),
                request.status(),
                request.tradeStartDate(),
                request.note()
        );
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND);
        }
        customerRepository.deleteById(id);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Transactional
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * 채번 규칙의 inputMode 에 따라 최종 코드 결정.
     * AUTO: 항상 시스템 생성 / MANUAL: 사용자 입력 필수 + 패턴 검증 / AUTO_OR_MANUAL: 입력 있으면 검증, 없으면 생성.
     */
    private String resolveCode(String requested) {
        CodeRuleInfo rule = codeRuleApi.getRule(CodeRuleTarget.CUSTOMER);
        InputMode mode = rule.inputMode();
        boolean hasInput = requested != null && !requested.isBlank();

        if (mode == InputMode.AUTO || (mode == InputMode.AUTO_OR_MANUAL && !hasInput)) {
            return codeRuleApi.generate(CodeRuleTarget.CUSTOMER, CodeGenerationContext.empty());
        }
        if (!hasInput) {
            throw new BusinessException(CustomerErrorCode.CODE_REQUIRED);
        }
        String trimmed = requested.trim();
        codeRuleApi.validate(CodeRuleTarget.CUSTOMER, trimmed);
        return trimmed;
    }

    private Address toAddress(String zipCode, String roadAddress, String detailAddress) {
        if (zipCode == null && roadAddress == null && detailAddress == null) {
            return null;
        }
        return Address.builder()
                .zipCode(zipCode)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
