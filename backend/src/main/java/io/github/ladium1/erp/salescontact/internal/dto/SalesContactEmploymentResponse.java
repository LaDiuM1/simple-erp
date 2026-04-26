package io.github.ladium1.erp.salescontact.internal.dto;

import io.github.ladium1.erp.salescontact.internal.entity.DepartureType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SalesContactEmploymentResponse(
        Long id,
        Long contactId,
        String contactName,
        Long customerId,
        String customerName,
        String externalCompanyName,
        String position,
        String department,
        LocalDate startDate,
        LocalDate endDate,
        DepartureType departureType,
        String departureNote,
        boolean active
) {
}
