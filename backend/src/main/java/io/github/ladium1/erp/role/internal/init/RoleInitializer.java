package io.github.ladium1.erp.role.internal.init;

import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.role.internal.entity.Role;
import io.github.ladium1.erp.role.internal.entity.RoleMenu;
import io.github.ladium1.erp.role.internal.repository.RoleMenuRepository;
import io.github.ladium1.erp.role.internal.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 서버 실행 시 권한 테스트 데이터 시드 — 비시스템 역할이 하나도 없을 때만.
 * <p>
 * MASTER 시스템 역할은 EmployeeInitializer 가 관리자 계정 부트스트랩 시 함께 생성한다 (system=true).
 * 이곳은 사용자가 자유롭게 편집/삭제할 수 있는 역할만 시드.
 */
@Slf4j
@Component
@Order(100) // 의존 없음 — Employee (Order=200) 의 선결.
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationRunner {

    private static final String SEED_MARKER_CODE = "SALES";

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (roleRepository.existsByCode(SEED_MARKER_CODE)) {
            return;
        }

        log.info("권한 테스트 데이터 초기화 시작");

        Map<Menu, Perm> salesPerms = new EnumMap<>(Menu.class);
        salesPerms.put(Menu.CUSTOMERS, perm(true, true));
        salesPerms.put(Menu.SALES_CONTACTS, perm(true, true));
        salesPerms.put(Menu.SALES_CUSTOMERS, perm(true, true));
        salesPerms.put(Menu.EMPLOYEES, perm(true, false));
        salesPerms.put(Menu.DEPARTMENTS, perm(true, false));
        salesPerms.put(Menu.POSITIONS, perm(true, false));
        save("SALES", "영업담당", "고객사 / 영업 명부 / 영업 활동 관리", salesPerms);

        Map<Menu, Perm> hrPerms = new EnumMap<>(Menu.class);
        hrPerms.put(Menu.EMPLOYEES, perm(true, true));
        hrPerms.put(Menu.DEPARTMENTS, perm(true, true));
        hrPerms.put(Menu.POSITIONS, perm(true, true));
        hrPerms.put(Menu.ROLES, perm(true, false));
        save("HR", "인사담당", "직원 / 부서 / 직책 관리", hrPerms);

        Map<Menu, Perm> viewerPerms = new EnumMap<>(Menu.class);
        viewerPerms.put(Menu.EMPLOYEES, perm(true, false));
        viewerPerms.put(Menu.DEPARTMENTS, perm(true, false));
        viewerPerms.put(Menu.POSITIONS, perm(true, false));
        viewerPerms.put(Menu.CUSTOMERS, perm(true, false));
        viewerPerms.put(Menu.SALES_CONTACTS, perm(true, false));
        viewerPerms.put(Menu.SALES_CUSTOMERS, perm(true, false));
        save("VIEWER", "조회자", "전 메뉴 읽기 전용", viewerPerms);

        log.info("권한 테스트 데이터 3건 생성 완료");
    }

    private void save(String code, String name, String description, Map<Menu, Perm> permissions) {
        Role role = roleRepository.save(Role.builder()
                .code(code)
                .name(name)
                .description(description)
                .system(false)
                .build());

        List<RoleMenu> rows = permissions.entrySet().stream()
                .map(entry -> RoleMenu.builder()
                        .role(role)
                        .menuCode(entry.getKey())
                        .canRead(entry.getValue().canRead())
                        .canWrite(entry.getValue().canWrite())
                        .build())
                .toList();
        roleMenuRepository.saveAll(rows);
    }

    private static Perm perm(boolean canRead, boolean canWrite) {
        return new Perm(canRead, canWrite);
    }

    private record Perm(boolean canRead, boolean canWrite) {
    }
}
