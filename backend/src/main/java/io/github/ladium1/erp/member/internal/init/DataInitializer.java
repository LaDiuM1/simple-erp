package io.github.ladium1.erp.member.internal.init;

import io.github.ladium1.erp.member.internal.entity.Member;
import io.github.ladium1.erp.member.internal.entity.MemberStatus;
import io.github.ladium1.erp.member.internal.repository.MemberRepository;
import io.github.ladium1.erp.menu.api.MenuApi;
import io.github.ladium1.erp.menu.api.dto.MenuInfo;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.RoleCreateRequest;
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
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final RoleApi roleApi;
    private final MenuApi menuApi;
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
        if (memberRepository.existsByLoginId(adminLoginId)) {
            return;
        }

        log.info("최초 관리자 계정 초기화 시작");

        // 2. 관리자 권한 조회 후 없으면 생성
        RoleInfo masterRole = roleApi.findByCode("MASTER")
                .orElseGet(() -> {
                    // 관리자 권한 생성
                    RoleInfo newRole = roleApi.createRole(RoleCreateRequest.builder()
                            .code("MASTER")
                            .name("관리자")
                            .description("시스템 전체 관리 권한")
                            .build());

                    // 관리자 계정에 모든 메뉴의 권한 할당
                    roleApi.assignMenuPermissions(
                            newRole.id(),
                            menuApi.getAllMenus()
                                    .stream()
                                    .map(MenuInfo::id)
                                    .toList()
                    );

                    return newRole;
                });

        // 3. 관리자 계정 생성
        memberRepository.save(Member.builder()
                .loginId(adminLoginId)
                .password(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .joinDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .roleId(masterRole.id())
                .build());

        log.info("관리자 계정 생성 완료");
    }
}