package io.github.ladium1.erp.contract.internal.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ContractPaymentResponse(
        Long id,
        String label,
        LocalDate plannedDate,
        Long plannedAmount,
        LocalDate paidDate,
        Long paidAmount,
        LocalDate invoiceDate,
        Long invoiceAmount,
        String note
) {
}
