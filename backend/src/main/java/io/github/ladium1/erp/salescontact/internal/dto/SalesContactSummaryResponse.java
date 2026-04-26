package io.github.ladium1.erp.salescontact.internal.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SalesContactSummaryResponse(
        Long id,
        String name,
        String mobilePhone,
        String email,
        String currentCompanyName,
        String currentPosition,
        String currentDepartment,
        LocalDate metAt
) {
}
