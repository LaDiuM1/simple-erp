package io.github.ladium1.erp.department.internal.repository;

import io.github.ladium1.erp.department.internal.dto.DepartmentSearchCondition;
import io.github.ladium1.erp.department.internal.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentRepositoryCustom {

    Page<Department> search(DepartmentSearchCondition condition, Pageable pageable);
}
