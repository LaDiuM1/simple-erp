package io.github.ladium1.erp.role.internal.dto;

import io.github.ladium1.erp.role.api.dto.MenuPermission;
import lombok.Builder;

import java.util.List;

@Builder
public record RoleDetailResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean system,
        /**
         * 메뉴별 권한 매트릭스. 매뉴얼별로 read=false/write=false 도 포함한 전체 메뉴 행을 반환한다 (FE 매트릭스 렌더 편의).
         */
        List<MenuPermission> menuPermissions
) {
}
