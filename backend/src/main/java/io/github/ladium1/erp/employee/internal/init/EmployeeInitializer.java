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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeInitializer implements ApplicationRunner {

    private static final List<String> TEST_EMPLOYEE_NAMES = List.of(
            "김민수", "이지영", "박서준", "최수빈", "정도현",
            "강하늘", "윤지호", "임서연", "한예린", "오재민",
            "신유진", "조성민", "배지우", "송하윤", "권나연",
            "황준혁", "안수현", "홍태윤", "노은서", "유시우",
            "전민재", "고아영", "문채원", "양도윤", "손지아",
            "백현우", "허지훈", "남예진", "심다은", "장우빈"
    );

    private static final List<EmployeeStatus> TEST_EMPLOYEE_STATUS_CYCLE = List.of(
            EmployeeStatus.ACTIVE, EmployeeStatus.ACTIVE, EmployeeStatus.ACTIVE,
            EmployeeStatus.ACTIVE, EmployeeStatus.ACTIVE, EmployeeStatus.LEAVE,
            EmployeeStatus.ACTIVE, EmployeeStatus.ACTIVE, EmployeeStatus.RESIGNED,
            EmployeeStatus.ACTIVE
    );

    private static final String TEST_EMPLOYEE_PASSWORD = "password123";

    private final EmployeeRepository employeeRepository;
    private final RoleApi roleApi;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-login-id}")
    private String adminLoginId;

    @Value("${app.admin-password}")
    private String adminPassword;

    // 서버 실행 시 최초 관리자 계정 초기화 작업
    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        // 1. 관리자 계정 존재 시 스킵
        if (employeeRepository.existsByLoginId(adminLoginId)) {
            return;
        }

        log.info("최초 관리자 계정 초기화 시작");

        // 2. 관리자 권한 부트스트랩 — 없으면 system=true + 모든 메뉴 read/write 자동 부여
        RoleInfo masterRole = roleApi.bootstrapSystemRole(
                "MASTER", "관리자", "시스템 전체 관리 권한"
        );

        // 3. 관리자 계정 생성
        employeeRepository.save(Employee.builder()
                .loginId(adminLoginId)
                .password(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .joinDate(LocalDate.now())
                .status(EmployeeStatus.ACTIVE)
                .roleId(masterRole.id())
                .build());

        log.info("관리자 계정 생성 완료");

        // 4. 테스트 직원 데이터 생성
        createTestEmployees(masterRole.id());
    }

    private void createTestEmployees(Long roleId) {
        String encodedPassword = passwordEncoder.encode(TEST_EMPLOYEE_PASSWORD);

        for (int i = 0; i < TEST_EMPLOYEE_NAMES.size(); i++) {
            int idx = i + 1;
            String loginId = String.format("user%02d", idx);
            String email = String.format("%s@simple-erp.io", loginId);
            String phone = String.format("010-%04d-%04d", 1000 + (idx * 37) % 9000, 2000 + (idx * 53) % 8000);
            LocalDate joinDate = LocalDate.of(2020 + (idx % 6), 1 + (idx % 12), 1 + (idx % 28));
            EmployeeStatus status = TEST_EMPLOYEE_STATUS_CYCLE.get(i % TEST_EMPLOYEE_STATUS_CYCLE.size());

            employeeRepository.save(Employee.builder()
                    .loginId(loginId)
                    .password(encodedPassword)
                    .name(TEST_EMPLOYEE_NAMES.get(i))
                    .email(email)
                    .phone(phone)
                    .joinDate(joinDate)
                    .status(status)
                    .roleId(roleId)
                    .build());
        }

        log.info("테스트 직원 {}명 생성 완료", TEST_EMPLOYEE_NAMES.size());
    }
}
