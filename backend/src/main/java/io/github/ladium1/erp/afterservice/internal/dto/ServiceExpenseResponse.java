package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ExpensePayerType;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceExpenseCategory;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ServiceExpenseResponse(
        Long id,
        ServiceExpenseCategory category,
        Long amount,
        ExpensePayerType payerType,
        LocalDate paidDate,
        Long engineerId,
        String engineerName,
        String note
) {
}
