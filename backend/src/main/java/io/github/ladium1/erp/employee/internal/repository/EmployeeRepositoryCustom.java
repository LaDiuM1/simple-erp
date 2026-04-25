package io.github.ladium1.erp.employee.internal.repository;

import io.github.ladium1.erp.employee.internal.dto.EmployeeSearchCondition;
import io.github.ladium1.erp.employee.internal.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface EmployeeRepositoryCustom {

    Page<Employee> search(EmployeeSearchCondition condition, Pageable pageable);

    List<Employee> searchAll(EmployeeSearchCondition condition, Sort sort);
}
