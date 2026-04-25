package io.github.ladium1.erp.employee.api.dto;

import lombok.Builder;

@Builder
public record EmployeeInfo(
        Long id,
        String loginId,
        String name,
        String departmentName,
        String positionName
) {
}
