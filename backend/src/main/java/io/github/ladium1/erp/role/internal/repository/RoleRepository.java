package io.github.ladium1.erp.role.internal.repository;

import io.github.ladium1.erp.role.internal.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, RoleRepositoryCustom {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);
}
