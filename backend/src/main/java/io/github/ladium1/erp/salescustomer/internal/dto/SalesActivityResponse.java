package io.github.ladium1.erp.salescustomer.internal.dto;

import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivityType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SalesActivityResponse(
        Long id,
        Long customerId,
        String customerName,
        SalesActivityType type,
        LocalDateTime activityDate,
        String subject,
        String content,
        Long ourEmployeeId,
        String ourEmployeeName,
        String ourEmployeeDepartmentName,
        Long customerContactId,
        String customerContactRegisteredName,
        String customerContactPosition
) {
}
