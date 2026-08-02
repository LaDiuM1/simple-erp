package io.github.ladium1.erp.employee.internal.dto;

import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import lombok.Builder;

@Builder
public record EmployeeReferenceResponse(
        Long id,
        String name,
        String departmentName,
        String positionName,
        EmployeeStatus status
) {
}
