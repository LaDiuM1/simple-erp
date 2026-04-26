package io.github.ladium1.erp.role.internal.repository;

import io.github.ladium1.erp.role.internal.entity.Role;
import io.github.ladium1.erp.role.internal.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    List<RoleMenu> findAllByRole(Role role);

    void deleteAllByRole(Role role);
}
