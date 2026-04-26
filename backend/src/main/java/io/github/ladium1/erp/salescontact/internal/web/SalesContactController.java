package io.github.ladium1.erp.salescontact.internal.web;

import io.github.ladium1.erp.global.web.PageResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return salesContactService.search(
                new SalesContactSearchCondition(nameKeyword, emailKeyword, phoneKeyword),
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public SalesContactDetailResponse getDetail(@PathVariable Long id) {
        return salesContactService.getDetail(id);
    }

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
