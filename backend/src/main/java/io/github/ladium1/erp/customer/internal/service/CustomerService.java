package io.github.ladium1.erp.customer.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.CustomerVisibilityContributor;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.customer.api.dto.RecentCustomerInfo;
import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerDetailResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerSearchCondition;
import io.github.ladium1.erp.customer.internal.dto.CustomerSummaryResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerUpdateRequest;
import io.github.ladium1.erp.customer.internal.entity.Address;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import io.github.ladium1.erp.customer.internal.excel.CustomerExcelExporter;
import io.github.ladium1.erp.customer.internal.excel.CustomerExcelImporter;
import io.github.ladium1.erp.customer.internal.excel.CustomerExcelImporter.Holder;
import io.github.ladium1.erp.customer.internal.exception.CustomerErrorCode;
import io.github.ladium1.erp.customer.internal.mapper.CustomerMapper;
import io.github.ladium1.erp.customer.internal.repository.CustomerRepository;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.excel.ExcelImporter.ParsedRow;
import io.github.ladium1.erp.global.excel.ExcelImporter.ParsedRows;
import io.github.ladium1.erp.global.excel.ExcelRowError;
import io.github.ladium1.erp.global.excel.ExcelUploadResult;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.security.DataScopeResolver;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements CustomerApi {

    /**
     * 가시성 결정에 함께 고려하는 메뉴들 — Customer 컨트롤러가 두 메뉴 모두에서 진입 가능 (CAN_READ_REFERENCE).
     * 두 메뉴 중 더 permissive 한 스코프 적용.
     */
    private static final Menu[] VISIBILITY_MENUS = { Menu.CUSTOMERS, Menu.SALES_CUSTOMERS };

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CodeRuleApi codeRuleApi;
    private final CustomerExcelExporter customerExcelExporter;
    private final CustomerExcelImporter customerExcelImporter;
    private final Validator validator;
    private final DataScopeResolver dataScopeResolver;
    private final DataScopeContextProvider dataScopeContextProvider;
    private final List<CustomerVisibilityContributor> visibilityContributors;

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

    @Override
    public long count() {
        return customerRepository.count();
    }

    @Override
    public List<RecentCustomerInfo> findRecent(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return customerRepository.findAll(pageable).stream()
                .map(c -> RecentCustomerInfo.builder()
                        .id(c.getId())
                        .code(c.getCode())
                        .name(c.getName())
                        .type(c.getType().name())
                        .status(c.getStatus().name())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }

    public PageResponse<CustomerSummaryResponse> search(CustomerSearchCondition condition, Pageable pageable) {
        Optional<Set<Long>> visible = resolveVisibleIds();
        if (visible.isPresent() && visible.get().isEmpty()) {
            return PageResponse.of(Page.empty(pageable));
        }
        CustomerSearchCondition scoped = visible.map(condition::withIdScope).orElse(condition);
        Page<Customer> page = customerRepository.search(scoped, pageable);
        return PageResponse.of(page.map(customerMapper::toSummaryResponse));
    }

    public byte[] exportExcel(CustomerSearchCondition condition, Sort sort) {
        Optional<Set<Long>> visible = resolveVisibleIds();
        if (visible.isPresent() && visible.get().isEmpty()) {
            return customerExcelExporter.export(List.of());
        }
        CustomerSearchCondition scoped = visible.map(condition::withIdScope).orElse(condition);
        List<Customer> customers = customerRepository.searchAll(scoped, sort);
        List<CustomerSummaryResponse> rows = customers.stream()
                .map(customerMapper::toSummaryResponse)
                .toList();
        return customerExcelExporter.export(rows);
    }

    /**
     * 엑셀 업로드용 빈 템플릿 — 다운로드 헤더 / 폭 / 톤 그대로, 데이터 행 없음.
     */
    public byte[] exportTemplate() {
        return customerExcelImporter.exportTemplate();
    }

    /**
     * 엑셀 업로드 — all-or-nothing. 한 행이라도 검증 실패 시 전체 롤백 + 에러 행 정보 반환.
     */
    @Transactional
    public ExcelUploadResult importExcel(MultipartFile file) {
        ParsedRows<Holder> parsed = customerExcelImporter.parse(file);
        List<ExcelRowError> errors = new ArrayList<>(parsed.errors());

        Set<String> seenCodes = new HashSet<>();
        Set<String> seenBizRegNos = new HashSet<>();

        for (ParsedRow<Holder> pr : parsed.builders()) {
            int rowNum = pr.rowNum();
            Holder holder = pr.builder();
            CustomerCreateRequest req = holder.toRequest();

            for (ConstraintViolation<CustomerCreateRequest> v : validator.validate(req)) {
                errors.add(ExcelRowError.of(rowNum, v.getPropertyPath().toString(), v.getMessage()));
            }

            String code;
            try {
                code = resolveCode(holder.code, holder.type);
            } catch (BusinessException e) {
                errors.add(ExcelRowError.of(rowNum, "고객사 코드", e.getMessage()));
                continue;
            }
            holder.code = code;

            if (!seenCodes.add(code)) {
                errors.add(ExcelRowError.of(rowNum, "고객사 코드", "엑셀 내에서 중복된 코드입니다."));
            } else if (customerRepository.existsByCode(code)) {
                errors.add(ExcelRowError.of(rowNum, "고객사 코드", CustomerErrorCode.DUPLICATE_CODE.getMessage()));
            }

            String bizRegNo = trimToNull(holder.bizRegNo);
            if (bizRegNo != null) {
                if (!seenBizRegNos.add(bizRegNo)) {
                    errors.add(ExcelRowError.of(rowNum, "사업자등록번호", "엑셀 내에서 중복된 사업자등록번호입니다."));
                } else if (customerRepository.existsByBizRegNo(bizRegNo)) {
                    errors.add(ExcelRowError.of(rowNum, "사업자등록번호", CustomerErrorCode.DUPLICATE_BIZ_REG_NO.getMessage()));
                }
                holder.bizRegNo = bizRegNo;
            }
        }

        if (!errors.isEmpty()) {
            // 시퀀스가 이미 증가했더라도 트랜잭션 롤백으로 복구.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ExcelUploadResult.failure(parsed.totalRows(), errors);
        }

        for (ParsedRow<Holder> pr : parsed.builders()) {
            Holder h = pr.builder();
            Customer customer = Customer.builder()
                    .code(h.code)
                    .name(h.name)
                    .bizRegNo(h.bizRegNo)
                    .representative(h.representative)
                    .phone(h.phone)
                    .email(h.email)
                    .address(toAddress(null, h.roadAddress, null))
                    .type(h.type)
                    .status(h.status)
                    .tradeStartDate(h.tradeStartDate)
                    .build();
            customerRepository.save(customer);
        }
        return ExcelUploadResult.success(parsed.totalRows());
    }

    public CustomerDetailResponse getDetail(Long id) {
        assertVisible(id);
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

    @Auditable(menu = Menu.CUSTOMERS, action = AuditAction.CREATE, targetType = "Customer", targetIdFromReturn = true)
    @Transactional
    public Long create(CustomerCreateRequest request) {
        String code = resolveCode(request.code(), request.type());

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

    @Auditable(menu = Menu.CUSTOMERS, action = AuditAction.UPDATE, targetType = "Customer", targetIdParam = "id")
    @Transactional
    public void update(Long id, CustomerUpdateRequest request) {
        assertVisible(id);
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

    @Auditable(menu = Menu.CUSTOMERS, action = AuditAction.DELETE, targetType = "Customer", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        assertVisible(id);
        if (!customerRepository.existsById(id)) {
            throw new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND);
        }
        customerRepository.deleteById(id);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Auditable(menu = Menu.CUSTOMERS, action = AuditAction.DELETE, targetType = "Customer")
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
     * <p>
     * 코드 규칙이 {TYPE} 토큰을 사용하면 customer.type 이 분류값으로 채번에 전달된다.
     */
    private String resolveCode(String requested, CustomerType type) {
        CodeRuleInfo rule = codeRuleApi.getRule(CodeRuleTarget.CUSTOMER);
        InputMode mode = rule.inputMode();
        boolean hasInput = requested != null && !requested.isBlank();

        if (mode == InputMode.AUTO || (mode == InputMode.AUTO_OR_MANUAL && !hasInput)) {
            Map<String, String> attrs = type != null ? Map.of("TYPE", type.name()) : Map.of();
            return codeRuleApi.generate(CodeRuleTarget.CUSTOMER, CodeGenerationContext.withAttributes(attrs));
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

    /**
     * 데이터 스코프 적용 후 가시 customer 식별자 집합을 산출.
     * - {@code Optional.empty()} = ALL (제한 없음)
     * - {@code Optional.of(empty)} = 보이는 행 0 건 (서비스가 빈 결과로 분기)
     * - {@code Optional.of(set)} = 그 ID 집합만 보임
     */
    private Optional<Set<Long>> resolveVisibleIds() {
        DataScope scope = dataScopeResolver.resolveMostPermissive(VISIBILITY_MENUS);
        if (scope == DataScope.ALL) {
            return Optional.empty();
        }
        DataScopeContext ctx = dataScopeContextProvider.current();
        Set<Long> union = visibilityContributors.stream()
                .flatMap(c -> c.visibleCustomerIds(scope, ctx).stream())
                .collect(Collectors.toSet());
        return Optional.of(union);
    }

    /**
     * 단건 가시성 강제 — 가시 집합에 없는 ID 는 존재 자체를 노출하지 않기 위해 NOT_FOUND 로 처리.
     */
    private void assertVisible(Long customerId) {
        Optional<Set<Long>> visible = resolveVisibleIds();
        if (visible.isPresent() && !visible.get().contains(customerId)) {
            throw new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND);
        }
    }
}
