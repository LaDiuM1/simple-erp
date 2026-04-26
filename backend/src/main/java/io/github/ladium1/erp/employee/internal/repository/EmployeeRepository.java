package io.github.ladium1.erp.employee.internal.repository;

import io.github.ladium1.erp.employee.internal.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, EmployeeRepositoryCustom {

    // 로그인 시 퇴사자를 제외한 로그인 ID 확인
    @Query("""
           select m from Employee m
           where m.loginId = :loginId
           and m.status != EmployeeStatus.RESIGNED
           """)
    Optional<Employee> findNotResignedByLoginId(String loginId);

    Optional<Employee> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByDepartmentId(Long departmentId);

    boolean existsByPositionId(Long positionId);

}