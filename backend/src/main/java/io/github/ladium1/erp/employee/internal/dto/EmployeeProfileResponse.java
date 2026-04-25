package io.github.ladium1.erp.employee.internal.dto;

import io.github.ladium1.erp.role.api.dto.MenuPermission;
import lombok.Builder;

import java.util.List;

@Builder
public record EmployeeProfileResponse(
        Long id,
        String loginId,
        String name,
        String departmentName,
        String positionName,
        String roleName,
        String roleCode,
        List<MenuPermission> menuPermissions
) {
}