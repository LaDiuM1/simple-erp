package io.github.ladium1.erp.role.api.dto;

import io.github.ladium1.erp.global.menu.Menu;

public record MenuPermission(
        Menu menuCode,
        boolean canRead,
        boolean canWrite
) {
}
