package io.github.ladium1.erp.expense.internal.dto;

import io.github.ladium1.erp.expense.internal.entity.ExpenseCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 경비 청구 생성 요청 — 생성 즉시 approverIds 결재선으로 상신.
 * <p>
 * items 의 비어 있음 검증은 서비스에서 EMPTY_ITEMS 로 처리 (도메인 에러 코드 통일).
 */
public record ExpenseCreateRequest(
        @NotBlank String title,
        @Valid List<ItemRequest> items,
        @NotEmpty List<Long> approverIds
) {

    public record ItemRequest(
            @NotNull LocalDate expenseDate,
            @NotNull ExpenseCategory category,
            @NotNull @Positive BigDecimal amount,
            String description,
            Long receiptFileId
    ) {
    }
}
