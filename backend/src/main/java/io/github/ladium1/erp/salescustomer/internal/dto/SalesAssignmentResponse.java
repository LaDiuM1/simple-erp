package io.github.ladium1.erp.salescustomer.internal.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SalesAssignmentResponse(
        Long id,
        Long customerId,
        Long employeeId,
        String employeeName,
        String employeeDepartmentName,
        String employeePositionName,
        LocalDate startDate,
        LocalDate endDate,
        boolean primary,
        String reason,
        boolean active
) {
}
