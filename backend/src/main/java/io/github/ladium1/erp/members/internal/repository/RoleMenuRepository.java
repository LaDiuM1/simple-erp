package io.github.ladium1.erp.members.internal.repository;

import io.github.ladium1.erp.members.internal.entity.Role;
import io.github.ladium1.erp.members.internal.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {
    List<RoleMenu> findByRole(Role role);
}