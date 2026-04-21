package io.github.ladium1.erp.role;

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
    RoleInfo createRole(RoleCreateRequest request);    /**

     *
     * 특정 권한에 대해 주어진 메뉴에 대한 읽기, 쓰기 권한 할당
     */
    void assignMenuPermissions(Long roleId, List<Long> menuIds);
}