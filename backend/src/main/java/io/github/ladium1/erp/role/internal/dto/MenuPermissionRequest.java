package io.github.ladium1.erp.role.internal.dto;

import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;
import jakarta.validation.constraints.NotNull;

/**
 * 매트릭스 한 행에 해당하는 메뉴 권한 요청 항목.
 * canWrite=true 인데 canRead=false 면 service 단에서 canRead=true 로 자동 보정.
 * dataScope 미지정 (null) 은 ALL 로 보정.
 */
public record MenuPermissionRequest(
        @NotNull
        Menu menuCode,

        boolean canRead,

        boolean canWrite,

        DataScope dataScope
) {
    /**
     * dataScope 미지정 호환 — service 가 null 을 ALL 로 보정.
     * FE 가 dataScope 를 보내지 않거나 기존 테스트가 3-인자 호출로 남아있을 때 안전.
     */
    public MenuPermissionRequest(Menu menuCode, boolean canRead, boolean canWrite) {
        this(menuCode, canRead, canWrite, null);
    }
}
