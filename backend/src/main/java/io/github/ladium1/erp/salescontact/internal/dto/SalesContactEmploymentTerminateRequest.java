package io.github.ladium1.erp.salescontact.internal.dto;

import io.github.ladium1.erp.salescontact.internal.entity.DepartureType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SalesContactEmploymentTerminateRequest(
        @NotNull
        LocalDate endDate,

        @NotNull
        DepartureType departureType,

        @Size(max = 200)
        String departureNote
) {
}
