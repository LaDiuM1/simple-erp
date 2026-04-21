package io.github.ladium1.erp.member.internal.repository;

import io.github.ladium1.erp.member.internal.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}