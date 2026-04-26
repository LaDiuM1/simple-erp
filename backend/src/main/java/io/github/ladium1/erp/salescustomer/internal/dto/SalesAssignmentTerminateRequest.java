package io.github.ladium1.erp.salescustomer.internal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SalesAssignmentTerminateRequest(
        @NotNull
        LocalDate endDate,

        @Size(max = 200)
        String reason
) {
}
