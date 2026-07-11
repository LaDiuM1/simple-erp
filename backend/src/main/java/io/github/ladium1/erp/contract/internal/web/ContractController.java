package io.github.ladium1.erp.contract.internal.web;

import io.github.ladium1.erp.contract.internal.dto.ContractCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractDetailResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractNoteCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractPaymentRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.dto.ContractSummaryResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractUpdateRequest;
import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.service.ContractService;
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
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private static final String MENU_CODE = "CONTRACTS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final ContractService contractService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<ContractSummaryResponse> search(
            @RequestParam(required = false) String contractNoKeyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractDateTo,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return contractService.search(
                toCondition(contractNoKeyword, customerId, employeeId, supplierId, status, contractDateFrom, contractDateTo),
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public ContractDetailResponse getDetail(@PathVariable Long id) {
        return contractService.getDetail(id);
    }

    @GetMapping("/excel")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(required = false) String contractNoKeyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractDateTo,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort
    ) {
        byte[] bytes = contractService.exportExcel(
                toCondition(contractNoKeyword, customerId, employeeId, supplierId, status, contractDateFrom, contractDateTo),
                sort
        );
        String filename = "contracts_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        return DownloadResponse.xlsx(bytes, filename);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody ContractCreateRequest request) {
        return contractService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody ContractUpdateRequest request) {
        contractService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        contractService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        contractService.deleteAll(ids);
    }

    @PostMapping("/{contractId}/payments")
    @PreAuthorize(CAN_WRITE)
    public Long createPayment(@PathVariable Long contractId,
                              @Valid @RequestBody ContractPaymentRequest request) {
        return contractService.createPayment(contractId, request);
    }

    @PutMapping("/payments/{id}")
    @PreAuthorize(CAN_WRITE)
    public void updatePayment(@PathVariable Long id,
                              @Valid @RequestBody ContractPaymentRequest request) {
        contractService.updatePayment(id, request);
    }

    @DeleteMapping("/payments/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deletePayment(@PathVariable Long id) {
        contractService.deletePayment(id);
    }

    @PostMapping("/{contractId}/notes")
    @PreAuthorize(CAN_WRITE)
    public Long createNote(@PathVariable Long contractId,
                           @Valid @RequestBody ContractNoteCreateRequest request) {
        return contractService.createNote(contractId, request);
    }

    @DeleteMapping("/notes/{id}")
    @PreAuthorize(CAN_WRITE)
    public void deleteNote(@PathVariable Long id) {
        contractService.deleteNote(id);
    }

    private ContractSearchCondition toCondition(String contractNoKeyword, Long customerId, Long employeeId,
                                                Long supplierId, ContractStatus status,
                                                LocalDate contractDateFrom, LocalDate contractDateTo) {
        return new ContractSearchCondition(contractNoKeyword, customerId, employeeId, supplierId,
                status, contractDateFrom, contractDateTo);
    }
}
