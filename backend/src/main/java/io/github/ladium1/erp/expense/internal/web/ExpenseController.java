package io.github.ladium1.erp.expense.internal.web;

import io.github.ladium1.erp.expense.internal.dto.ExpenseCreateRequest;
import io.github.ladium1.erp.expense.internal.dto.ExpenseDetailResponse;
import io.github.ladium1.erp.expense.internal.dto.ExpenseReceiptDownload;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchCondition;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchScope;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSummaryResponse;
import io.github.ladium1.erp.expense.internal.entity.ExpenseStatus;
import io.github.ladium1.erp.expense.internal.service.ExpenseService;
import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private static final String MENU_CODE = "EXPENSES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize(CAN_READ)
    public Long create(@AuthenticationPrincipal User user, @Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.create(user.getUsername(), request);
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<ExpenseSummaryResponse> search(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "MINE") ExpenseSearchScope scope,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return expenseService.search(
                user.getUsername(),
                scope,
                new ExpenseSearchCondition(status, startDate, endDate, keyword),
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public ExpenseDetailResponse getDetail(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return expenseService.getDetail(user.getUsername(), id);
    }

    @GetMapping("/{id}/receipts/{fileId}")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadReceipt(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @PathVariable Long fileId
    ) {
        ExpenseReceiptDownload download = expenseService.downloadReceipt(user.getUsername(), id, fileId);
        return DownloadResponse.attachment(download.content(), download.name(), download.contentType());
    }
}
