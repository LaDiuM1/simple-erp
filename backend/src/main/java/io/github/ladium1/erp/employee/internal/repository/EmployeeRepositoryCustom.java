package io.github.ladium1.erp.employee.internal.repository;

import io.github.ladium1.erp.employee.internal.dto.EmployeeSearchCondition;
import io.github.ladium1.erp.employee.internal.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public interface EmployeeRepositoryCustom {

    Page<Employee> search(EmployeeSearchCondition condition, Pageable pageable);

    /** null 은 전체, 빈 집합은 0건, 값이 있으면 해당 직원만 조회한다. */
    Page<Employee> searchVisible(
            EmployeeSearchCondition condition,
            Set<Long> visibleEmployeeIds,
            Pageable pageable
    );

    List<Employee> searchAll(EmployeeSearchCondition condition, Sort sort);
}
