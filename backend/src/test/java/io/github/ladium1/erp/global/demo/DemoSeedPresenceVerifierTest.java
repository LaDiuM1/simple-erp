package io.github.ladium1.erp.global.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DemoSeedPresenceVerifierTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DemoStateStore stateStore;
    @Mock private DemoFileGenerationVerifier fileGenerationVerifier;
    @Mock private DemoStartupVerificationGate startupVerificationGate;
    @Mock private ApplicationArguments arguments;

    private DemoProperties properties;
    private DemoSeedPresenceVerifier verifier;

    @BeforeEach
    void setUp() {
        properties = new DemoProperties();
        properties.setEnabled(true);
        properties.getSeed().setExpectedVersion("seed-v1");
        verifier = new DemoSeedPresenceVerifier(
                jdbcTemplate,
                passwordEncoder,
                properties,
                stateStore,
                fileGenerationVerifier,
                startupVerificationGate
        );

        lenient().when(stateStore.current()).thenReturn(validStatus());
        lenient().when(passwordEncoder.matches("manager-password", "manager-hash")).thenReturn(true);
        lenient().when(passwordEncoder.matches("staff-password", "staff-hash")).thenReturn(true);
        given(jdbcTemplate.queryForList(anyString())).willAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("demo_seed_manifest")) return validManifest();
            if (sql.contains("FROM roles")) return validRoles();
            if (sql.contains("FROM employees")) return validAccounts();
            throw new AssertionError("예상하지 못한 SQL: " + sql);
        });
    }

    @Test
    @DisplayName("manifest 1행, 필수 역할, 활성 데모 계정과 비밀번호가 일치하면 통과")
    void valid_canonical_seed_passes() {
        assertThatCode(() -> verifier.run(arguments)).doesNotThrowAnyException();

        verify(fileGenerationVerifier).verify(validStatus(), "seed-v1", 30);
        verify(startupVerificationGate).markSeedVerified();
    }

    @Test
    @DisplayName("manifest가 없으면 reference/admin bootstrap 전에 fail-fast")
    void missing_manifest_fails() {
        reset(jdbcTemplate);
        willReturn(List.of()).given(jdbcTemplate).queryForList(anyString());

        assertThatThrownBy(() -> verifier.run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("demo_seed_manifest는 정확히 1행");

        verify(startupVerificationGate, never()).markSeedVerified();
    }

    @Test
    @DisplayName("데모 계정의 DB 역할이 status 계약과 다르면 fail-fast")
    void account_role_mismatch_fails() {
        reset(jdbcTemplate);
        given(jdbcTemplate.queryForList(anyString())).willAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("demo_seed_manifest")) return validManifest();
            if (sql.contains("FROM roles")) return validRoles();
            return List.of(
                    account("demo.manager", "manager-hash", "ACTIVE", "DEMO_STAFF"),
                    account("demo.staff", "staff-hash", "ACTIVE", "DEMO_STAFF")
            );
        });

        assertThatThrownBy(() -> verifier.run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("상태·역할·비밀번호 계약 불일치: demo.manager");

        verify(startupVerificationGate, never()).markSeedVerified();
    }

    @Test
    @DisplayName("파일 세대 검증에 실패하면 startup 쓰기 gate를 열지 않음")
    void file_generation_failure_keeps_startup_gate_closed() {
        willThrow(new IllegalStateException("file generation mismatch"))
                .given(fileGenerationVerifier).verify(validStatus(), "seed-v1", 30);

        assertThatThrownBy(() -> verifier.run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("file generation mismatch");

        verify(startupVerificationGate, never()).markSeedVerified();
    }

    @Test
    @DisplayName("status가 검증되지 않은 데모 계정을 더 노출하면 fail-fast")
    void unexpected_announced_account_fails() {
        given(stateStore.current()).willReturn(new DemoStatusResponse(
                true, "DEMO", DemoState.VERIFYING,
                OffsetDateTime.parse("2026-08-02T12:00:00+09:00"),
                "generation-1", "generation-2", null, null,
                300, 120, true, "점검", false,
                new DemoSimulatedLocation(37.5663, 126.9779),
                List.of(
                        new DemoPublicAccount("관리자", "전체", "demo.manager", "manager-password", true),
                        new DemoPublicAccount("직원", "개인", "demo.staff", "staff-password", false),
                        new DemoPublicAccount("미검증", "추가", "demo.extra", "extra-password", false)
                )
        ));

        assertThatThrownBy(() -> verifier.run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("데모 계정 집합이 canonical 설정과 다름");
    }

    private DemoStatusResponse validStatus() {
        return new DemoStatusResponse(
                true, "DEMO", DemoState.VERIFYING,
                OffsetDateTime.parse("2026-08-02T12:00:00+09:00"),
                "generation-1", "generation-2", null, null,
                300, 120, true, "점검", false,
                new DemoSimulatedLocation(37.5663, 126.9779),
                List.of(
                        new DemoPublicAccount("관리자", "전체", "demo.manager", "manager-password", true),
                        new DemoPublicAccount("직원", "개인", "demo.staff", "staff-password", false)
                ));
    }

    private List<Map<String, Object>> validManifest() {
        return List.of(Map.of(
                "id", 1L,
                "seed_version", "seed-v1",
                "schema_version", "schema-v1",
                "scenario_version", "scenario-v1",
                "generated_at", Instant.parse("2026-08-02T03:00:00Z")
        ));
    }

    private List<Map<String, Object>> validRoles() {
        return List.of(
                Map.of("code", "MASTER", "system_flag", true),
                Map.of("code", "DEMO_MANAGER", "system_flag", false),
                Map.of("code", "DEMO_STAFF", "system_flag", false)
        );
    }

    private List<Map<String, Object>> validAccounts() {
        return List.of(
                account("demo.manager", "manager-hash", "ACTIVE", "DEMO_MANAGER"),
                account("demo.staff", "staff-hash", "ACTIVE", "DEMO_STAFF")
        );
    }

    private Map<String, Object> account(String loginId, String password, String status, String roleCode) {
        return Map.of(
                "login_id", loginId,
                "password", password,
                "status", status,
                "role_code", roleCode
        );
    }
}
