package io.github.ladium1.erp.salescontact.internal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * customerId 또는 externalCompanyName 둘 중 하나는 채워져야 함 — service 에서 검증.
 */
public record SalesContactEmploymentCreateRequest(
        Long customerId,

        @Size(max = 200)
        String externalCompanyName,

        @Size(max = 100)
        String position,

        @Size(max = 100)
        String department,

        @NotNull
        LocalDate startDate
) {
}
