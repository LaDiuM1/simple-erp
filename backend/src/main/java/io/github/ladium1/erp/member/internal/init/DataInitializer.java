package io.github.ladium1.erp.member.internal.init;

import io.github.ladium1.erp.member.internal.entity.Member;
import io.github.ladium1.erp.member.internal.entity.MemberStatus;
import io.github.ladium1.erp.member.internal.entity.Role;
import io.github.ladium1.erp.member.internal.entity.RoleMenu;
import io.github.ladium1.erp.member.internal.repository.MemberRepository;
import io.github.ladium1.erp.member.internal.repository.RoleMenuRepository;
import io.github.ladium1.erp.member.internal.repository.RoleRepository;
import io.github.ladium1.erp.menu.MenuApi;
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
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final PasswordEncoder passwordEncoder;

    private final MenuApi menuApi;

    @Value("${app.admin-login-id}")
    private String adminLoginId;

    @Value("${app.admin-password}")
    private String adminPassword;

    // 서버 실행 시 최초 관리자 계정 초기화 작업
    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        // 1. 관리자 계정 존재 시 스킵
        if (memberRepository.existsByLoginId(adminLoginId)) {
            return;
        }

        log.info("최초 관리자 계정 초기화 시작");

        // 2. MASTER 권한 조회 또는 생성
        Role masterRole = roleRepository.findByCode("MASTER")
                .orElseGet(() -> {
                    Role newRole = roleRepository.save(
                            Role.builder().code("MASTER").name("관리자").build()
                    );
                    initMasterPermissions(newRole);
                    return newRole;
                });

        // 3. 관리자 계정 생성
        memberRepository.save(Member.builder()
                .loginId(adminLoginId)
                .password(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .joinDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .role(masterRole)
                .build());

        log.info("관리자 계정 생성 완료");
    }

    private void initMasterPermissions(Role role) {
        List<RoleMenu> roleMenus = menuApi.getAllMenus().stream()
                .map(menu -> RoleMenu.builder()
                        .menuId(menu.id())
                        .role(role)
                        .canRead(true)
                        .canWrite(true)
                        .build())
                .toList();

        roleMenuRepository.saveAll(roleMenus);
    }

}