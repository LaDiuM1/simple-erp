package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DemoProtectionPolicyTest {

    @Mock
    private DemoStateStore stateStore;

    private DemoProperties properties;
    private DemoProtectionPolicy policy;

    @BeforeEach
    void setUp() {
        properties = new DemoProperties();
        policy = new DemoProtectionPolicy(properties, stateStore);
    }

    @Test
    @DisplayName("demo off는 기존 업로드·직원·역할·IP·좌표 동작을 유지")
    void disabled_mode_preserves_existing_behavior() {
        assertThatCode(policy::assertUploadAllowed).doesNotThrowAnyException();
        assertThat(policy.shouldRetainStoredFiles()).isFalse();
        assertThatCode(() -> policy.assertEmployeeDeletionAllowed("demo.manager")).doesNotThrowAnyException();
        assertThatCode(() -> policy.assertRoleMutationAllowed("MASTER")).doesNotThrowAnyException();
        assertThat(policy.auditIp("10.0.0.1")).isEqualTo("10.0.0.1");
        assertThat(policy.effectiveLocation(1.0, 2.0)).isEqualTo(new DemoSimulatedLocation(1.0, 2.0));
    }

    @Test
    @DisplayName("demo에서 보호 계정·역할과 파일 ID 첨부를 중앙 차단")
    void enabled_mode_protects_recovery_core() {
        properties.setEnabled(true);
        properties.getUpload().setEnabled(false);
        properties.getProtection().setOperationsAdminLoginId("private.ops");

        assertDemoError(() -> policy.assertEmployeeDeletionAllowed("demo.manager"),
                DemoErrorCode.DEMO_PROTECTED_RESOURCE);
        assertDemoError(() -> policy.assertProtectedEmployeeUpdateAllowed(
                        "demo.staff", true, false, false),
                DemoErrorCode.DEMO_PROTECTED_RESOURCE);
        assertDemoError(() -> policy.assertEmployeeDeletionAllowed("private.ops"),
                DemoErrorCode.DEMO_PROTECTED_RESOURCE);
        assertDemoError(() -> policy.assertRoleMutationAllowed("DEMO_MANAGER"),
                DemoErrorCode.DEMO_PROTECTED_RESOURCE);
        assertDemoError(() -> policy.assertNoAttachmentIds(List.of(10L)),
                DemoErrorCode.DEMO_UPLOAD_DISABLED);
        assertThat(policy.shouldRetainStoredFiles()).isTrue();
    }

    @Test
    @DisplayName("demo 업로드를 허용해도 파일 본체는 다음 reset까지 보존")
    void enabled_uploads_are_retained_until_reset() {
        properties.setEnabled(true);
        properties.getUpload().setEnabled(true);

        assertThatCode(policy::assertUploadAllowed).doesNotThrowAnyException();
        assertThatCode(() -> policy.assertNoAttachmentIds(List.of(10L))).doesNotThrowAnyException();
        assertThat(policy.shouldRetainStoredFiles()).isTrue();
    }

    @Test
    @DisplayName("demo privacy는 최종 저장 IP를 null로 만들고 서버 모의 좌표를 반환")
    void enabled_mode_applies_privacy_boundary() {
        properties.setEnabled(true);
        properties.getAudit().setStoreClientIp(false);
        properties.getGeolocation().setUseSimulatedPosition(true);
        properties.getGeolocation().setLatitude(37.5);
        properties.getGeolocation().setLongitude(127.0);

        assertThat(policy.auditIp("203.0.113.1")).isNull();
        assertThat(policy.effectiveLocation(1.0, 2.0))
                .isEqualTo(new DemoSimulatedLocation(37.5, 127.0));
    }

    @Test
    @DisplayName("복구 운영자 직원 정보는 본인에게만 보이고 일반 데모 계정에는 숨김")
    void operations_employee_visibility_is_private() {
        properties.setEnabled(true);
        properties.getProtection().setOperationsAdminLoginId("private.ops");

        assertThat(policy.recoveryOperationsEmployeeLoginId()).isEqualTo("private.ops");
        assertThat(policy.hiddenOperationsEmployeeLoginId("demo.manager")).isEqualTo("private.ops");
        assertThat(policy.isEmployeeHiddenFrom("demo.manager", "private.ops")).isTrue();
        assertThat(policy.isOperationsEmployee("private.ops")).isTrue();
        assertThat(policy.isOperationsEmployee("demo.manager")).isFalse();
        assertThat(policy.hiddenOperationsEmployeeLoginId("private.ops")).isNull();
        assertThat(policy.isEmployeeHiddenFrom("private.ops", "private.ops")).isFalse();
    }

    @Test
    @DisplayName("FAILED 또는 reset lock 상태에서는 안정 코드로 쓰기 차단")
    void unavailable_state_blocks_writes() {
        properties.setEnabled(true);
        given(stateStore.current()).willReturn(new DemoStatusResponse(
                true, "DEMO", DemoState.FAILED,
                OffsetDateTime.parse("2026-08-02T12:00:00+09:00"), null, null,
                null, null, 300, 120, true, "점검", false, null, List.of()));

        assertDemoError(policy::assertWriteAvailable, DemoErrorCode.DEMO_RESET_IN_PROGRESS);
    }

    private static void assertDemoError(Runnable operation, DemoErrorCode expected) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", expected);
    }
}
