package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ExpensePayerType;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceExpenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 경비 등록 / 수정 공용 요청 — 두 케이스의 입력 항목이 동일.
 */
public record ServiceExpenseRequest(
        @NotNull
        ServiceExpenseCategory category,

        @NotNull @PositiveOrZero
        Long amount,

        @NotNull
        ExpensePayerType payerType,

        LocalDate paidDate,

        Long engineerId,

        @Size(max = 255)
        String note
) {
}
