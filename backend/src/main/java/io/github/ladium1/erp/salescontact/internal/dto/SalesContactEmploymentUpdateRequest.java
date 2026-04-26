package io.github.ladium1.erp.salescontact.internal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SalesContactEmploymentUpdateRequest(
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
