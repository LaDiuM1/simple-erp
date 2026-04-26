package io.github.ladium1.erp.role.internal.dto;

import io.github.ladium1.erp.global.menu.Menu;
import jakarta.validation.constraints.NotNull;

/**
 * 매트릭스 한 행에 해당하는 메뉴 권한 요청 항목.
 * canWrite=true 인데 canRead=false 면 service 단에서 canRead=true 로 자동 보정.
 */
public record MenuPermissionRequest(
        @NotNull
        Menu menuCode,

        boolean canRead,

        boolean canWrite
) {
}
