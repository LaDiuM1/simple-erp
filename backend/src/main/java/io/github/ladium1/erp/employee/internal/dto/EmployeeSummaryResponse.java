package io.github.ladium1.erp.employee.internal.dto;

import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record EmployeeSummaryResponse(
        Long id,
        String loginId,
        String name,
        String departmentName,
        String positionName,
        String roleName,
        String email,
        String phone,
        LocalDate joinDate,
        EmployeeStatus status
) {
}
