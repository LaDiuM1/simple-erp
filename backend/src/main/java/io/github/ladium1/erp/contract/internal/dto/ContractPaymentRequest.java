package io.github.ladium1.erp.contract.internal.dto;

import io.github.ladium1.erp.global.validation.MoneyPolicy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
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

        @PositiveOrZero @Max(MoneyPolicy.MAX_AMOUNT)
        Long plannedAmount,

        LocalDate paidDate,

        @PositiveOrZero @Max(MoneyPolicy.MAX_AMOUNT)
        Long paidAmount,

        LocalDate invoiceDate,

        @PositiveOrZero @Max(MoneyPolicy.MAX_AMOUNT)
        Long invoiceAmount,

        @Size(max = 255)
        String note
) {
    @AssertTrue(message = "입금일과 입금액은 함께 입력해주세요.")
    public boolean isPaidPairComplete() {
        return (paidDate == null) == (paidAmount == null);
    }

    @AssertTrue(message = "계산서 발행일과 계산서 금액은 함께 입력해주세요.")
    public boolean isInvoicePairComplete() {
        return (invoiceDate == null) == (invoiceAmount == null);
    }
}
