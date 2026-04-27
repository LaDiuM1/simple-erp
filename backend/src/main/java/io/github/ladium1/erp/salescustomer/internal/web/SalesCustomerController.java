package io.github.ladium1.erp.salescustomer.internal.web;

import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityResponse;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentTerminateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerAggregate;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerDetailResponse;
import io.github.ladium1.erp.salescustomer.internal.service.SalesCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/sales-customers")
@RequiredArgsConstructor
public class SalesCustomerController {

    private static final String MENU_CODE = "SALES_CUSTOMERS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    // 명부 상세 페이지의 "활동 이력" 섹션이 소비 — 소비 페이지 권한으로 묶음.
    private static final String CONTACT_MENU_CODE = "SALES_CONTACTS";
    private static final String CONTACT_CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + CONTACT_MENU_CODE + "')";

    private final SalesCustomerService salesCustomerService;

    /**
     * 고객사별 활성 담당자 / 활동 집계 — 목록 페이지가 customer 마스터 페이지와 합성하기 위한 보강 데이터.
     */
    @GetMapping("/aggregates")
    @PreAuthorize(CAN_READ)
    public List<SalesCustomerAggregate> aggregates(@RequestParam List<Long> customerIds) {
        return salesCustomerService.aggregateByCustomerIds(customerIds);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize(CAN_READ)
    public SalesCustomerDetailResponse getDetail(@PathVariable Long customerId) {
        return salesCustomerService.getDetail(customerId);
    }

    /**
     * 영업 명부 상세 페이지가 호출 — 해당 명부 인물이 등장한 활동 이력 (모든 고객사 통합).
     */
    @GetMapping("/contacts/{contactId}/activities")
    @PreAuthorize(CONTACT_CAN_READ)
    public List<SalesActivityResponse> findActivitiesByContactId(@PathVariable Long contactId) {
        return salesCustomerService.findActivitiesByContactId(contactId);
    }

    @PostMapping("/activities")
    @PreAuthorize(CAN_WRITE)
    public Long createActivity(@Valid @RequestBody SalesActivityCreateRequest request) {
        return salesCustomerService.createActivity(request);
    }

    @PutMapping("/activities/{id}")
    @PreAuthorize(CAN_WRITE)
    public void updateActivity(@PathVariable Long id, @Valid @RequestBody SalesActivityUpdateRequest request) {
        salesCustomerService.updateActivity(id, request);
    }

    @DeleteMapping("/activities/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteActivity(@PathVariable Long id) {
        salesCustomerService.deleteActivity(id);
    }

    @PostMapping("/assignments")
    @PreAuthorize(CAN_WRITE)
    public Long createAssignment(@Valid @RequestBody SalesAssignmentCreateRequest request) {
        return salesCustomerService.createAssignment(request);
    }

    @PutMapping("/assignments/{id}")
    @PreAuthorize(CAN_WRITE)
    public void updateAssignment(@PathVariable Long id, @Valid @RequestBody SalesAssignmentUpdateRequest request) {
        salesCustomerService.updateAssignment(id, request);
    }

    @PutMapping("/assignments/{id}/terminate")
    @PreAuthorize(CAN_WRITE)
    public void terminateAssignment(@PathVariable Long id, @Valid @RequestBody SalesAssignmentTerminateRequest request) {
        salesCustomerService.terminateAssignment(id, request);
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteAssignment(@PathVariable Long id) {
        salesCustomerService.deleteAssignment(id);
    }
}
