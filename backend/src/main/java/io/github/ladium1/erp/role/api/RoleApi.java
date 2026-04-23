package io.github.ladium1.erp.role.api;

import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleCreateRequest;
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
     * 권한 생성
     */
    RoleInfo createRole(RoleCreateRequest request);

    /**
     * 특정 권한에 대해 주어진 메뉴에 대한 읽기, 쓰기 권한 할당
     */
    void assignMenuPermissions(Long roleId, List<Long> menuIds);

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