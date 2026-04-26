package io.github.ladium1.erp.role.api;

import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;

import java.util.List;
import java.util.Optional;

public interface RoleApi {

    /**
     * 권한 id로 권한 정보 반환
     */
    RoleInfo getById(Long id);

    /**
     * 권한 코드로 권한 정보 조회
     */
    Optional<RoleInfo> findByCode(String code);

    /**
     * 시스템 권한 부트스트랩 — system=true 로 생성하고 모든 메뉴에 대해 read/write 부여.
     * 초기화 전용 (Initializer 등). 이미 존재하면 그대로 반환.
     */
    RoleInfo bootstrapSystemRole(String code, String name, String description);

    /**
     * 권한 id로 권한에 대한 메뉴 권한 반환
     */
    List<MenuPermission> getMenuPermissionsByRoleId(Long roleId);

    /**
     * 전체 권한 목록 반환 (이름 오름차순)
     */
    List<RoleInfo> findAll();

    /**
     * 주어진 id 목록에 해당하는 권한 정보 반환
     */
    List<RoleInfo> findByIds(List<Long> ids);
}
