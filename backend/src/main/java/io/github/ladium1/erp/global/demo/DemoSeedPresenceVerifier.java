package io.github.ladium1.erp.global.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * reference/admin bootstrap보다 먼저 canonical seed의 최소 계약을 검증한다.
 * demo off에서는 bean 자체가 등록되지 않는다.
 */
@Slf4j
@Component
@Order(75)
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "demo",
        name = {"enabled", "seed.validation-enabled"},
        havingValue = "true"
)
public class DemoSeedPresenceVerifier implements ApplicationRunner {

    private static final String MANIFEST_SQL = """
            SELECT id, seed_version, schema_version, scenario_version, generated_at
            FROM demo_seed_manifest
            """;
    private static final String ROLES_SQL = "SELECT code, `system` AS system_flag FROM roles";
    private static final String ACCOUNTS_SQL = """
            SELECT e.login_id, e.password, e.status, r.code AS role_code
            FROM employees e
            LEFT JOIN roles r ON r.id = e.role_id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final DemoProperties properties;
    private final DemoStateStore stateStore;
    private final DemoFileGenerationVerifier fileGenerationVerifier;
    private final DemoStartupVerificationGate startupVerificationGate;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        String expectedVersion = properties.getSeed().getExpectedVersion();
        if (expectedVersion == null || expectedVersion.isBlank()) {
            throw invalid("demo.seed.expected-version이 비어 있음");
        }

        verifyManifest(expectedVersion);
        Map<String, RoleRow> roles = verifyRoles();
        DemoStatusResponse status = verifiedStatus();
        verifyPublicAccounts(roles, status);
        fileGenerationVerifier.verify(
                status,
                expectedVersion,
                properties.getSeed().getRequiredFileCount()
        );
        startupVerificationGate.markSeedVerified();
        log.info("데모 canonical seed 검증 완료: seedVersion={}", expectedVersion);
    }

    private void verifyManifest(String expectedVersion) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(MANIFEST_SQL);
        if (rows.size() != 1) {
            throw invalid("demo_seed_manifest는 정확히 1행이어야 함");
        }
        Map<String, Object> manifest = rows.getFirst();
        Object id = manifest.get("id");
        if (!(id instanceof Number number) || number.longValue() != 1L
                || !expectedVersion.equals(stringValue(manifest.get("seed_version")))
                || isBlank(stringValue(manifest.get("schema_version")))
                || isBlank(stringValue(manifest.get("scenario_version")))
                || manifest.get("generated_at") == null) {
            throw invalid("demo_seed_manifest 값이 canonical seed 계약과 다름");
        }
    }

    private Map<String, RoleRow> verifyRoles() {
        Map<String, RoleRow> roles = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(ROLES_SQL)) {
            String code = stringValue(row.get("code"));
            roles.putIfAbsent(code, new RoleRow(code, booleanValue(row.get("system_flag"))));
        }

        Set<String> requiredCodes = properties.getSeed().getRequiredRoleCodes();
        if (!roles.keySet().containsAll(requiredCodes)) {
            throw invalid("필수 데모 역할 누락: " + difference(requiredCodes, roles.keySet()));
        }
        RoleRow master = roles.get("MASTER");
        if (master == null || !master.system()) {
            throw invalid("MASTER 역할이 system=true가 아님");
        }
        return roles;
    }

    private DemoStatusResponse verifiedStatus() {
        DemoStatusResponse status = stateStore.current();
        if (!status.enabled() || status.state() == DemoState.FAILED) {
            throw invalid("상태 파일이 seed 검증에 사용할 수 없음");
        }
        return status;
    }

    private void verifyPublicAccounts(Map<String, RoleRow> roles, DemoStatusResponse status) {
        List<RequiredAccount> requiredAccounts = requiredAccounts();
        Map<String, RequiredAccount> requiredByLoginId = new LinkedHashMap<>();
        for (RequiredAccount required : requiredAccounts) {
            if (requiredByLoginId.putIfAbsent(required.loginId(), required) != null) {
                throw invalid("필수 데모 계정 설정 loginId 중복: " + required.loginId());
            }
        }

        Map<String, DemoPublicAccount> announced = new LinkedHashMap<>();
        for (DemoPublicAccount account : status.publicAccounts()) {
            if (account == null || isBlank(account.loginId())) {
                throw invalid("상태 파일 데모 계정 loginId 누락");
            }
            if (announced.putIfAbsent(account.loginId(), account) != null) {
                throw invalid("상태 파일 데모 계정 loginId 중복: " + account.loginId());
            }
        }
        if (!announced.keySet().equals(requiredByLoginId.keySet())) {
            throw invalid("상태 파일 데모 계정 집합이 canonical 설정과 다름");
        }

        Map<String, AccountRow> stored = new LinkedHashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(ACCOUNTS_SQL)) {
            AccountRow account = new AccountRow(
                    stringValue(row.get("login_id")),
                    stringValue(row.get("password")),
                    stringValue(row.get("status")),
                    stringValue(row.get("role_code"))
            );
            if (stored.putIfAbsent(account.loginId(), account) != null) {
                throw invalid("DB 데모 계정 loginId 중복: " + account.loginId());
            }
        }

        for (RequiredAccount required : requiredAccounts) {
            if (!roles.containsKey(required.roleCode())) {
                throw invalid("데모 계정 역할 누락: " + required.roleCode());
            }
            DemoPublicAccount publicAccount = announced.get(required.loginId());
            AccountRow account = stored.get(required.loginId());
            if (publicAccount == null || account == null) {
                throw invalid("필수 데모 계정 누락: " + required.loginId());
            }
            if (publicAccount.password() == null || publicAccount.password().isBlank()
                    || !"ACTIVE".equals(account.status())
                    || !required.roleCode().equals(account.roleCode())
                    || !passwordEncoder.matches(publicAccount.password(), account.passwordHash())) {
                throw invalid("데모 계정 상태·역할·비밀번호 계약 불일치: " + required.loginId());
            }
        }
    }

    private List<RequiredAccount> requiredAccounts() {
        return properties.getSeed().getRequiredAccounts().stream()
                .map(spec -> {
                    int separator = spec.lastIndexOf(':');
                    if (separator <= 0 || separator == spec.length() - 1) {
                        throw invalid("필수 계정 설정 형식 오류: " + spec);
                    }
                    return new RequiredAccount(spec.substring(0, separator), spec.substring(separator + 1));
                })
                .toList();
    }

    private static Set<String> difference(Set<String> required, Set<String> actual) {
        java.util.LinkedHashSet<String> missing = new java.util.LinkedHashSet<>(required);
        missing.removeAll(actual);
        return missing;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(stringValue(value));
    }

    private static IllegalStateException invalid(String reason) {
        return new IllegalStateException("데모 canonical seed 검증 실패: " + reason);
    }

    private record RoleRow(String code, boolean system) {
    }

    private record AccountRow(String loginId, String passwordHash, String status, String roleCode) {
    }

    private record RequiredAccount(String loginId, String roleCode) {
    }
}
