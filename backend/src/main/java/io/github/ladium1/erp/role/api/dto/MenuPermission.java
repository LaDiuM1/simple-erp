package io.github.ladium1.erp.role.api.dto;

import lombok.Builder;

@Builder
public record MenuPermission(
        Long menuId,
        String menuCode,
        boolean canRead,
        boolean canWrite
) {
}
