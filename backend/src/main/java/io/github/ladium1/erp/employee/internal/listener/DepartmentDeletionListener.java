package io.github.ladium1.erp.employee.internal.listener;

import io.github.ladium1.erp.department.api.DepartmentDeletingEvent;
import io.github.ladium1.erp.employee.internal.exception.EmployeeErrorCode;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 부서 삭제 시도 시 해당 부서 소속 직원이 있는지 검사한다.
 * 동기 EventListener — 예외를 던지면 발행 측 트랜잭션이 롤백되어 삭제가 거부된다.
 */
@Component
@RequiredArgsConstructor
public class DepartmentDeletionListener {

    private final EmployeeRepository employeeRepository;

    @EventListener
    public void on(DepartmentDeletingEvent event) {
        if (employeeRepository.existsByDepartmentId(event.departmentId())) {
            throw new BusinessException(EmployeeErrorCode.EMPLOYEE_EXISTS_IN_DEPARTMENT);
        }
    }
}
