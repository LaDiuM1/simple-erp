package io.github.ladium1.erp.global.audit.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.internal.entity.AuditLog;
import io.github.ladium1.erp.global.audit.internal.repository.AuditLogRepository;
import io.github.ladium1.erp.global.menu.Menu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("record 성공 — 모든 컬럼이 채워진 AuditLog 가 저장된다")
    void record_success() {
        // when
        auditService.record(
                Menu.EMPLOYEES, AuditAction.CREATE,
                "admin", 12L,
                "Employee", 42L,
                "a3f7c2d1", "10.0.1.5"
        );

        // then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getMenuCode()).isEqualTo(Menu.EMPLOYEES);
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(saved.getActorLoginId()).isEqualTo("admin");
        assertThat(saved.getActorId()).isEqualTo(12L);
        assertThat(saved.getTargetType()).isEqualTo("Employee");
        assertThat(saved.getTargetId()).isEqualTo(42L);
        assertThat(saved.getTraceId()).isEqualTo("a3f7c2d1");
        assertThat(saved.getIpAddress()).isEqualTo("10.0.1.5");
    }

    @Test
    @DisplayName("record — 빈 targetType 은 null 로 정규화")
    void record_blank_target_type_to_null() {
        // when
        auditService.record(
                Menu.ROLES, AuditAction.UPDATE,
                "SYSTEM", null,
                "  ", null,
                null, null
        );

        // then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getTargetType()).isNull();
    }

    @Test
    @DisplayName("record 실패 — repository 예외는 swallow (도메인 트랜잭션 영향 X)")
    void record_swallow_repository_exception() {
        // given
        given(auditLogRepository.save(any(AuditLog.class)))
                .willThrow(new RuntimeException("DB down"));

        // when & then — 예외가 호출자로 전파되지 않음
        auditService.record(
                Menu.EMPLOYEES, AuditAction.CREATE,
                "admin", null, null, null, null, null
        );

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
