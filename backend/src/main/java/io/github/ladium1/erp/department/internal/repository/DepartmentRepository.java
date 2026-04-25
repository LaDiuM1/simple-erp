package io.github.ladium1.erp.department.internal.repository;

import io.github.ladium1.erp.department.internal.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long>, DepartmentRepositoryCustom {

    boolean existsByCode(String code);

    boolean existsByParentId(Long parentId);
}
