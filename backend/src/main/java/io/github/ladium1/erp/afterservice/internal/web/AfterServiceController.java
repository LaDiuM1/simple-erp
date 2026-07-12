package io.github.ladium1.erp.afterservice.internal.web;

import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceCreateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceDetailResponse;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSummaryResponse;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceUpdateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceExpenseRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceVisitRequest;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import io.github.ladium1.erp.afterservice.internal.service.AfterServiceService;
import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/after-services")
@RequiredArgsConstructor
public class AfterServiceController {

    private static final String MENU_CODE = "AFTER_SERVICES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final AfterServiceService afterServiceService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<AfterServiceSummaryResponse> search(
            @RequestParam(required = false) String receiptNoKeyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) ServiceType type,
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(required = false) WarrantyDecision warrantyDecision,
            @RequestParam(required = false) Long engineerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate receivedDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate receivedDateTo,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return afterServiceService.search(
                new AfterServiceSearchCondition(receiptNoKeyword, customerId, type, status,
                        warrantyDecision, engineerId, receivedDateFrom, receivedDateTo),
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public AfterServiceDetailResponse getDetail(@PathVariable Long id) {
        return afterServiceService.getDetail(id);
    }

    @GetMapping("/excel")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(required = false) String receiptNoKeyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) ServiceType type,
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(required = false) WarrantyDecision warrantyDecision,
            @RequestParam(required = false) Long engineerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate receivedDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate receivedDateTo,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort
    ) {
        byte[] bytes = afterServiceService.exportExcel(
                new AfterServiceSearchCondition(receiptNoKeyword, customerId, type, status,
                        warrantyDecision, engineerId, receivedDateFrom, receivedDateTo),
                sort
        );
        String filename = "after-services_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        return DownloadResponse.xlsx(bytes, filename);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody AfterServiceCreateRequest request) {
        return afterServiceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody AfterServiceUpdateRequest request) {
        afterServiceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        afterServiceService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        afterServiceService.deleteAll(ids);
    }

    @PostMapping("/{afterServiceId}/visits")
    @PreAuthorize(CAN_WRITE)
    public Long createVisit(@PathVariable Long afterServiceId,
                            @Valid @RequestBody ServiceVisitRequest request) {
        return afterServiceService.createVisit(afterServiceId, request);
    }

    @PutMapping("/visits/{id}")
    @PreAuthorize(CAN_WRITE)
    public void updateVisit(@PathVariable Long id,
                            @Valid @RequestBody ServiceVisitRequest request) {
        afterServiceService.updateVisit(id, request);
    }

    @DeleteMapping("/visits/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteVisit(@PathVariable Long id) {
        afterServiceService.deleteVisit(id);
    }

    @PostMapping("/{afterServiceId}/expenses")
    @PreAuthorize(CAN_WRITE)
    public Long createExpense(@PathVariable Long afterServiceId,
                              @Valid @RequestBody ServiceExpenseRequest request) {
        return afterServiceService.createExpense(afterServiceId, request);
    }

    @PutMapping("/expenses/{id}")
    @PreAuthorize(CAN_WRITE)
    public void updateExpense(@PathVariable Long id,
                              @Valid @RequestBody ServiceExpenseRequest request) {
        afterServiceService.updateExpense(id, request);
    }

    @DeleteMapping("/expenses/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteExpense(@PathVariable Long id) {
        afterServiceService.deleteExpense(id);
    }
}
