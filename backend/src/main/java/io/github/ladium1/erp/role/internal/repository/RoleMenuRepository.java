package io.github.ladium1.erp.role.internal.repository;

import io.github.ladium1.erp.role.internal.entity.Role;
import io.github.ladium1.erp.role.internal.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    List<RoleMenu> findAllByRole(Role role);

    /**
     * 즉시 bulk DELETE 발행. derived `deleteAllBy...` 는 entity remove 큐잉만 해서 flush 시점에
     * INSERT 가 먼저 나가 (role_id, menu_code) unique 제약을 치는 케이스가 있어 명시적 JPQL 로 고정.
     */
    @Modifying
    @Query("DELETE FROM RoleMenu rm WHERE rm.role = :role")
    void deleteAllByRole(Role role);
}
