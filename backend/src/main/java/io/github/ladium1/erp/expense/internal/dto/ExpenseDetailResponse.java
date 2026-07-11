package io.github.ladium1.erp.expense.internal.dto;

import io.github.ladium1.erp.expense.internal.entity.ExpenseCategory;
import io.github.ladium1.erp.expense.internal.entity.ExpenseStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ExpenseDetailResponse(
        Long id,
        String title,
        BigDecimal totalAmount,
        ExpenseStatus status,
        String claimantName,
        Long approvalDocumentId,
        LocalDateTime createdAt,
        List<ItemResponse> items
) {

    @Builder
    public record ItemResponse(
            Long id,
            LocalDate expenseDate,
            ExpenseCategory category,
            BigDecimal amount,
            String description,
            Long receiptFileId,
            String receiptFileName
    ) {
    }
}
