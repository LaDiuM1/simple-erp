package io.github.ladium1.erp.role.api.dto;

import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;

public record MenuPermission(
        Menu menuCode,
        boolean canRead,
        boolean canWrite,
        DataScope dataScope
) {
    /**
     * dataScope 미지정 호환 — 기본 ALL.
     */
    public MenuPermission(Menu menuCode, boolean canRead, boolean canWrite) {
        this(menuCode, canRead, canWrite, DataScope.ALL);
    }
}
