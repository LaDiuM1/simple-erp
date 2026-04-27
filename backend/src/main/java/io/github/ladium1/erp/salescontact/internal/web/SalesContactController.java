package io.github.ladium1.erp.salescontact.internal.web;

import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.salescontact.internal.dto.AvailabilityResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactDetailResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentTerminateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSearchCondition;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSummaryResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.service.SalesContactService;
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
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-contacts")
@RequiredArgsConstructor
public class SalesContactController {

    private static final String MENU_CODE = "SALES_CONTACTS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    /**
     * 고객사 영업 관리에서 고객사별 재직 명부 섹션이 호출 — 양쪽 메뉴 read 권한 중 하나라도 허용.
     */
    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'SALES_CUSTOMERS')";

    private final SalesContactService salesContactService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<SalesContactSummaryResponse> search(
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) String emailKeyword,
            @RequestParam(required = false) String phoneKeyword,
            @RequestParam(required = false) List<Long> sourceIds,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return salesContactService.search(
                new SalesContactSearchCondition(nameKeyword, emailKeyword, phoneKeyword, sourceIds),
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public SalesContactDetailResponse getDetail(@PathVariable Long id) {
        return salesContactService.getDetail(id);
    }

    @GetMapping("/mobile-phone-availability")
    @PreAuthorize(CAN_WRITE)
    public AvailabilityResponse checkMobilePhoneAvailability(
            @RequestParam String mobilePhone,
            @RequestParam(required = false) Long excludeId
    ) {
        return new AvailabilityResponse(salesContactService.isMobilePhoneAvailable(mobilePhone, excludeId));
    }

    @GetMapping("/excel")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) String emailKeyword,
            @RequestParam(required = false) String phoneKeyword,
            @RequestParam(required = false) List<Long> sourceIds,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort
    ) {
        byte[] bytes = salesContactService.exportExcel(
                new SalesContactSearchCondition(nameKeyword, emailKeyword, phoneKeyword, sourceIds),
                sort
        );
        String filename = "sales-contacts_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        return DownloadResponse.xlsx(bytes, filename);
    }

    /**
     * 고객사 영업 관리 페이지의 "재직 명부" 섹션에서 호출 — 영업 명부 메뉴 권한이 없어도
     * 고객사 영업 메뉴 read 권한만으로 접근 가능하도록 CAN_READ_REFERENCE 적용.
     */
    @GetMapping("/employments")
    @PreAuthorize(CAN_READ_REFERENCE)
    public List<SalesContactEmploymentResponse> findEmploymentsByCustomerId(@RequestParam Long customerId) {
        return salesContactService.findEmploymentsByCustomerId(customerId);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody SalesContactCreateRequest request) {
        return salesContactService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody SalesContactUpdateRequest request) {
        salesContactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        salesContactService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        salesContactService.deleteAll(ids);
    }

    @PostMapping("/{contactId}/employments")
    @PreAuthorize(CAN_WRITE)
    public Long createEmployment(@PathVariable Long contactId,
                                 @Valid @RequestBody SalesContactEmploymentCreateRequest request) {
        return salesContactService.createEmployment(contactId, request);
    }

    @PutMapping("/employments/{id}")
    @PreAuthorize(CAN_WRITE)
    public void updateEmployment(@PathVariable Long id,
                                 @Valid @RequestBody SalesContactEmploymentUpdateRequest request) {
        salesContactService.updateEmployment(id, request);
    }

    @PutMapping("/employments/{id}/terminate")
    @PreAuthorize(CAN_WRITE)
    public void terminateEmployment(@PathVariable Long id,
                                    @Valid @RequestBody SalesContactEmploymentTerminateRequest request) {
        salesContactService.terminateEmployment(id, request);
    }

    @DeleteMapping("/employments/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteEmployment(@PathVariable Long id) {
        salesContactService.deleteEmployment(id);
    }
}
