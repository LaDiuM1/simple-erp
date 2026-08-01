package io.github.ladium1.erp.role.internal.dto;

import io.github.ladium1.erp.global.menu.Menu;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoleCreateRequest(
        @NotBlank @Size(max = 50)
        String code,

        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Size(max = Menu.MAX_PERMISSION_COUNT)
        List<@NotNull @Valid MenuPermissionRequest> menuPermissions
) {
}
