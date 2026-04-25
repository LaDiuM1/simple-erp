package io.github.ladium1.erp.employee.internal.dto;

import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record EmployeeDetailResponse(
        Long id,
        String loginId,
        String name,
        String email,
        String phone,
        String zipCode,
        String roadAddress,
        String detailAddress,
        LocalDate joinDate,
        EmployeeStatus status,
        Long departmentId,
        String departmentName,
        Long positionId,
        String positionName,
        Long roleId,
        String roleName
) {
}
