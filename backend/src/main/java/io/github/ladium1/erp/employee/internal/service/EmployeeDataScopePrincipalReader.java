package io.github.ladium1.erp.employee.internal.service;

import io.github.ladium1.erp.employee.internal.entity.Employee;
import io.github.ladium1.erp.employee.internal.exception.EmployeeErrorCode;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.DataScopePrincipal;
import io.github.ladium1.erp.global.security.DataScopePrincipalReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeDataScopePrincipalReader implements DataScopePrincipalReader {

    private final EmployeeRepository employeeRepository;

    @Override
    public DataScopePrincipal getRequiredByLoginId(String loginId) {
        return findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Override
    public Optional<DataScopePrincipal> findByLoginId(String loginId) {
        return employeeRepository.findByLoginId(loginId)
                .map(EmployeeDataScopePrincipalReader::toPrincipal);
    }

    private static DataScopePrincipal toPrincipal(Employee employee) {
        return new DataScopePrincipal(
                employee.getId(),
                employee.getRoleId(),
                employee.getDepartmentId()
        );
    }
}
