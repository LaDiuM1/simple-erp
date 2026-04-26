package io.github.ladium1.erp.role.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoleUpdateRequest(
        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        /**
         * 매트릭스 전체. 시스템 권한(system=true) 의 경우 service 가 무시한다.
         */
        @Valid
        List<MenuPermissionRequest> menuPermissions
) {
}
