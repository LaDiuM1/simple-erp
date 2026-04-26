package io.github.ladium1.erp.salescustomer.internal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SalesAssignmentCreateRequest(
        @NotNull
        Long customerId,

        @NotNull
        Long employeeId,

        @NotNull
        LocalDate startDate,

        boolean primary,

        @Size(max = 200)
        String reason
) {
}
