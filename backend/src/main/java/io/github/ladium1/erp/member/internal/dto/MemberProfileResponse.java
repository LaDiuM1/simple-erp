package io.github.ladium1.erp.member.internal.dto;

import io.github.ladium1.erp.role.api.dto.MenuPermission;
import lombok.Builder;

import java.util.List;

@Builder
public record MemberProfileResponse(
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