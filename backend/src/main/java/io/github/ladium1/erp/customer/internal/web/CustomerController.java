package io.github.ladium1.erp.customer.internal.web;

import io.github.ladium1.erp.customer.internal.dto.AvailabilityResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerDetailResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerReferenceResponse;
import io.github.ladium1.erp.customer.internal.dto.SalesCustomerReferenceResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerSearchCondition;
import io.github.ladium1.erp.customer.internal.dto.CustomerSummaryResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerUpdateRequest;
import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import io.github.ladium1.erp.customer.internal.service.CustomerService;
import io.github.ladium1.erp.global.excel.ExcelUploadResult;
import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private static final String MENU_CODE = "CUSTOMERS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    /** 관리 상세가 아닌 고객사 식별 정보가 필요한 업무 메뉴의 참조 조회 권한. */
    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'SALES_CONTACTS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'CONTRACTS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'EQUIPMENTS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'AFTER_SERVICES')";
    private static final String CAN_READ_SALES =
            "@menuPermissionEvaluator.canRead(authentication, 'SALES_CUSTOMERS')";

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<CustomerSummaryResponse> search(
            @RequestParam(required = false) String codeKeyword,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) String addressKeyword,
            @RequestParam(required = false) String phoneKeyword,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return customerService.search(toCondition(codeKeyword, nameKeyword, addressKeyword, phoneKeyword, type, status), pageable);
    }

    @GetMapping("/reference")
    @PreAuthorize(CAN_READ_REFERENCE)
    public PageResponse<CustomerReferenceResponse> searchReference(
            @RequestParam(required = false) String codeKeyword,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) String addressKeyword,
            @RequestParam(required = false) String phoneKeyword,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return customerService.searchReference(
                toCondition(codeKeyword, nameKeyword, addressKeyword, phoneKeyword, type, status),
                pageable
        );
    }

    @GetMapping("/sales-reference")
    @PreAuthorize(CAN_READ_SALES)
    public PageResponse<SalesCustomerReferenceResponse> searchSalesReference(
            @RequestParam(required = false) String codeKeyword,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) String addressKeyword,
            @RequestParam(required = false) String phoneKeyword,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return customerService.searchSalesReference(
                toCondition(codeKeyword, nameKeyword, addressKeyword, phoneKeyword, type, status),
                pageable
        );
    }

    @GetMapping("/sales-reference/{id}")
    @PreAuthorize(CAN_READ_SALES)
    public SalesCustomerReferenceResponse getSalesReference(@PathVariable Long id) {
        return customerService.getSalesReference(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public CustomerDetailResponse getDetail(@PathVariable Long id) {
        return customerService.getDetail(id);
    }

    @GetMapping("/excel")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(required = false) String codeKeyword,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) String addressKeyword,
            @RequestParam(required = false) String phoneKeyword,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(required = false) CustomerStatus status,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort
    ) {
        byte[] bytes = customerService.exportExcel(
                toCondition(codeKeyword, nameKeyword, addressKeyword, phoneKeyword, type, status),
                sort
        );
        String filename = "customers_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        return DownloadResponse.xlsx(bytes, filename);
    }

    @GetMapping("/excel/template")
    @PreAuthorize(CAN_WRITE)
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        byte[] bytes = customerService.exportTemplate();
        return DownloadResponse.xlsx(bytes, "customers_template.xlsx");
    }

    @PostMapping("/excel/upload")
    @PreAuthorize(CAN_WRITE)
    public ExcelUploadResult uploadExcel(@RequestPart("file") MultipartFile file) {
        return customerService.importExcel(file);
    }

    @GetMapping("/code-availability")
    @PreAuthorize(CAN_WRITE)
    public AvailabilityResponse checkCodeAvailability(@RequestParam String code) {
        return new AvailabilityResponse(customerService.isCodeAvailable(code));
    }

    @GetMapping("/bizregno-availability")
    @PreAuthorize(CAN_WRITE)
    public AvailabilityResponse checkBizRegNoAvailability(@RequestParam String bizRegNo) {
        return new AvailabilityResponse(customerService.isBizRegNoAvailable(bizRegNo));
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody CustomerCreateRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
        customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        customerService.deleteAll(ids);
    }

    private CustomerSearchCondition toCondition(String codeKeyword, String nameKeyword, String addressKeyword, String phoneKeyword, CustomerType type, CustomerStatus status) {
        return new CustomerSearchCondition(codeKeyword, nameKeyword, addressKeyword, phoneKeyword, type, status);
    }
}
