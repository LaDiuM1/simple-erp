package io.github.ladium1.erp.employee.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.TokenProvider;
import io.github.ladium1.erp.employee.internal.dto.LoginRequest;
import io.github.ladium1.erp.employee.internal.dto.TokenResponse;
import io.github.ladium1.erp.employee.internal.entity.Employee;
import io.github.ladium1.erp.employee.internal.exception.EmployeeErrorCode;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final RoleApi roleApi;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public TokenResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findNotResignedByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new BusinessException(EmployeeErrorCode.INVALID_PASSWORD);
        }

        RoleInfo employeeRoleInfo = roleApi.getById(employee.getRoleId());
        String token = tokenProvider.createToken(employee.getLoginId(), employeeRoleInfo.code());

        return new TokenResponse(token);
    }
}