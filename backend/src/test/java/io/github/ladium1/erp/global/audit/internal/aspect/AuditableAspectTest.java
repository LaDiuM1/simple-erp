package io.github.ladium1.erp.global.audit.internal.aspect;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.audit.internal.service.AuditService;
import io.github.ladium1.erp.global.menu.Menu;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditableAspectTest {

    @Mock private AuditService auditService;

    private TestTarget target;

    @BeforeEach
    void setUp() {
        AuditableAspect aspect = new AuditableAspect(auditService);
        AspectJProxyFactory factory = new AspectJProxyFactory(new TestTarget());
        factory.addAspect(aspect);
        target = factory.getProxy();
    }

    @AfterEach
    void clearThreadLocals() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("미인증 컨텍스트 — actor=SYSTEM 으로 record 호출")
    void unauthenticated_actor_is_system() {
        // when
        target.create();

        // then
        verify(auditService).record(
                eq(Menu.EMPLOYEES), eq(AuditAction.CREATE),
                eq("SYSTEM"), isNull(),
                eq("Employee"), eq(42L),
                isNull(), isNull()
        );
    }

    @Test
    @DisplayName("인증된 사용자 — actor_login_id 가 username 으로 채워진다")
    void authenticated_actor_login_id() {
        // given
        authenticate("admin");

        // when
        target.create();

        // then
        verify(auditService).record(
                eq(Menu.EMPLOYEES), eq(AuditAction.CREATE),
                eq("admin"), isNull(),
                eq("Employee"), eq(42L),
                isNull(), isNull()
        );
    }

    @Test
    @DisplayName("targetIdFromReturn — 반환값을 target_id 로 추출")
    void resolves_target_id_from_return() {
        // when
        target.create();

        // then
        verify(auditService).record(
                eq(Menu.EMPLOYEES), eq(AuditAction.CREATE),
                eq("SYSTEM"), isNull(),
                eq("Employee"), eq(42L),
                isNull(), isNull()
        );
    }

    @Test
    @DisplayName("targetIdParam — 인자 이름 매핑으로 target_id 추출")
    void resolves_target_id_from_param() {
        // when
        target.update(7L);

        // then
        verify(auditService).record(
                eq(Menu.EMPLOYEES), eq(AuditAction.UPDATE),
                eq("SYSTEM"), isNull(),
                eq("Employee"), eq(7L),
                isNull(), isNull()
        );
    }

    @Test
    @DisplayName("targetId 미지정 — null")
    void target_id_null_when_unspecified() {
        // when
        target.bulkAction();

        // then
        verify(auditService).record(
                eq(Menu.EMPLOYEES), eq(AuditAction.DELETE),
                eq("SYSTEM"), isNull(),
                eq("Employee"), isNull(),
                isNull(), isNull()
        );
    }

    @Test
    @DisplayName("MDC traceId — record 의 traceId 인자로 전달된다")
    void mdc_trace_id_is_passed() {
        // given
        MDC.put("traceId", "a3f7c2d1");

        // when
        target.create();

        // then
        verify(auditService).record(
                eq(Menu.EMPLOYEES), eq(AuditAction.CREATE),
                eq("SYSTEM"), isNull(),
                eq("Employee"), eq(42L),
                eq("a3f7c2d1"), isNull()
        );
    }

    @Test
    @DisplayName("도메인 메서드가 예외를 던지면 aspect 는 record 를 호출하지 않는다")
    void no_record_when_method_throws() {
        // when & then
        try {
            target.failing();
        } catch (RuntimeException ignored) {
        }

        // record 호출되지 않음
        verify(auditService, org.mockito.Mockito.never())
                .record(eq(Menu.EMPLOYEES), eq(AuditAction.CREATE),
                        eq("SYSTEM"), isNull(),
                        eq("Employee"), isNull(),
                        isNull(), isNull());
    }

    private static void authenticate(String username) {
        User principal = new User(username, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities())
        );
    }

    static class TestTarget {

        @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.CREATE, targetType = "Employee", targetIdFromReturn = true)
        public Long create() {
            return 42L;
        }

        @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.UPDATE, targetType = "Employee", targetIdParam = "id")
        public void update(Long id) {
        }

        @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.DELETE, targetType = "Employee")
        public void bulkAction() {
        }

        @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.CREATE, targetType = "Employee")
        public void failing() {
            throw new RuntimeException("boom");
        }
    }
}
