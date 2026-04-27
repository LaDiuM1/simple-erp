package io.github.ladium1.erp.department.internal.init;

import io.github.ladium1.erp.department.internal.entity.Department;
import io.github.ladium1.erp.department.internal.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 실행 시 부서 테스트 데이터 시드.
 * 부서가 하나도 없을 때만 본부 3개 + 각 본부 산하 팀 3개씩, 총 12개 부서를 생성한다.
 */
@Slf4j
@Component
@Order(100) // 의존 없음 — Employee (Order=200) 의 선결.
@RequiredArgsConstructor
public class DepartmentInitializer implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (departmentRepository.count() > 0) {
            return;
        }

        log.info("부서 테스트 데이터 초기화 시작");

        // 1단계: 본부
        Department managementHQ = save("HQ_MGMT", "경영지원본부", null);
        Department salesHQ = save("HQ_SALES", "영업본부", null);
        Department techHQ = save("HQ_TECH", "기술본부", null);

        // 2단계: 팀
        save("HR", "인사팀", managementHQ);
        save("FIN", "재무팀", managementHQ);
        save("GA", "총무팀", managementHQ);

        save("SALES1", "영업1팀", salesHQ);
        save("SALES2", "영업2팀", salesHQ);
        save("MKT", "마케팅팀", salesHQ);

        save("DEV", "개발팀", techHQ);
        save("INFRA", "인프라팀", techHQ);
        save("QA", "QA팀", techHQ);

        log.info("부서 테스트 데이터 12건 생성 완료");
    }

    private Department save(String code, String name, Department parent) {
        return departmentRepository.save(
            Department.builder()
                .code(code)
                .name(name)
                .parent(parent)
                .build()
        );
    }
}
