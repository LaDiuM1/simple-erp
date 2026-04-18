package io.github.ladium1.erp.members.internal.repository;

import io.github.ladium1.erp.members.internal.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
}