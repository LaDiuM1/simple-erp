package io.github.ladium1.erp.employee.internal.dto;

import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;

public record EmployeeSearchCondition(
        String keyword,
        Long departmentId,
        Long positionId,
        Long roleId,
        EmployeeStatus status
) {
}
