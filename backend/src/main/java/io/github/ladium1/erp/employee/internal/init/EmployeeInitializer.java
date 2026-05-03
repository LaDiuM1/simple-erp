package io.github.ladium1.erp.employee.internal.init;

import io.github.ladium1.erp.employee.internal.entity.Employee;
import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final RoleApi roleApi;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-login-id}")
    private String adminLoginId;

    @Value("${app.admin-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        // MASTER 시스템 권한은 매 부팅 reconcile — 신규 부팅 시 생성, 기존 부팅 시 누락 메뉴 행을 채워
        // Menu enum 추가/제거에도 권한 매트릭스가 자동 동기화되도록 함.
        RoleInfo masterRole = roleApi.bootstrapSystemRole(
                "MASTER", "관리자", "시스템 전체 관리 권한"
        );

        // 관리자 계정 부트스트랩은 최초 1회만 — 이미 존재하면 skip (idempotent).
        if (employeeRepository.existsByLoginId(adminLoginId)) {
            return;
        }

        log.info("최초 관리자 계정 초기화 시작");
        employeeRepository.save(Employee.builder()
                .loginId(adminLoginId)
                .password(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .joinDate(LocalDate.now())
                .status(EmployeeStatus.ACTIVE)
                .roleId(masterRole.id())
                .build());
        log.info("관리자 계정 생성 완료");
    }
}
