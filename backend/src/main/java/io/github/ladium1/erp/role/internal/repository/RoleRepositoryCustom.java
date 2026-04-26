package io.github.ladium1.erp.role.internal.repository;

import io.github.ladium1.erp.role.internal.dto.RoleSearchCondition;
import io.github.ladium1.erp.role.internal.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleRepositoryCustom {

    Page<Role> search(RoleSearchCondition condition, Pageable pageable);
}
