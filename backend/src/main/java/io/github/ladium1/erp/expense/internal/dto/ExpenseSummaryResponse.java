package io.github.ladium1.erp.expense.internal.dto;

import io.github.ladium1.erp.expense.internal.entity.ExpenseStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ExpenseSummaryResponse(
        Long id,
        String title,
        BigDecimal totalAmount,
        ExpenseStatus status,
        String claimantName,
        Long approvalDocumentId,
        LocalDateTime createdAt,
        int itemCount
) {
}
