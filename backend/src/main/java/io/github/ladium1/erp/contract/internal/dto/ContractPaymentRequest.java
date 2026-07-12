package io.github.ladium1.erp.contract.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 대금 회차 등록 / 수정 공용 요청 — 두 케이스의 입력 항목이 동일.
 */
public record ContractPaymentRequest(
        @NotBlank @Size(max = 50)
        String label,

        LocalDate plannedDate,

        @PositiveOrZero
        Long plannedAmount,

        LocalDate paidDate,

        @PositiveOrZero
        Long paidAmount,

        LocalDate invoiceDate,

        @PositiveOrZero
        Long invoiceAmount,

        @Size(max = 255)
        String note
) {
}
