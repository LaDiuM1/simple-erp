package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.web.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemoStateStoreTest {

    private static final Instant NOW = Instant.parse("2026-08-02T03:00:00Z");
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private DemoProperties properties;
    private Path statePath;

    @BeforeEach
    void setUp() {
        statePath = tempDir.resolve("state.json");
        properties = new DemoProperties();
        properties.setEnabled(true);
        properties.setStatePath(statePath.toString());
        properties.getUpload().setEnabled(false);
        properties.getGeolocation().setUseSimulatedPosition(true);
        properties.getGeolocation().setLatitude(37.5663);
        properties.getGeolocation().setLongitude(126.9779);
    }

    @Test
    @DisplayName("상태 파일이 없으면 FAILED + write lock 으로 fail-closed")
    void missing_file_is_failed_and_locked() {
        DemoStatusResponse status = store().current();

        assertThat(status.state()).isEqualTo(DemoState.FAILED);
        assertThat(status.writeLocked()).isTrue();
        assertThat(status.publicAccounts()).isEmpty();
    }

    @Test
    @DisplayName("정상 envelope의 data를 읽고 opaque generation과 데모 계정을 보존")
    void reads_complete_envelope() throws Exception {
        write(readyStatus(now().plusHours(6), false));

        DemoStatusResponse status = store().current();

        assertThat(status.state()).isEqualTo(DemoState.READY);
        assertThat(status.generation()).isEqualTo("opaque-generation-1");
        assertThat(status.writeLocked()).isFalse();
        assertThat(status.publicAccounts()).extracting(DemoPublicAccount::loginId)
                .containsExactly("demo.manager", "demo.staff");
    }

    @Test
    @DisplayName("nextResetAt 2분 경계부터 파일 플래그와 무관하게 쓰기를 잠금")
    void derives_scheduled_write_lock() throws Exception {
        write(readyStatus(now().plusSeconds(120), false));

        assertThat(store().current().writeLocked()).isTrue();
    }

    @Test
    @DisplayName("READY 상태가 nextResetAt을 넘기면 FAILED로 전환")
    void expired_ready_state_is_failed() throws Exception {
        write(readyStatus(now().minusSeconds(1), false));

        assertThat(store().current().state()).isEqualTo(DemoState.FAILED);
        assertThat(store().current().writeLocked()).isTrue();
    }

    @Test
    @DisplayName("RESETTING 상태 파일이 operation timeout을 넘기면 FAILED로 전환")
    void stale_maintenance_state_is_failed() throws Exception {
        write(status(
                DemoState.RESETTING,
                null,
                true,
                now().minusSeconds(20 * 60 + 1)
        ));

        assertThat(store().current().state()).isEqualTo(DemoState.FAILED);
    }

    @Test
    @DisplayName("stateChangedAt이 허용 오차보다 미래면 FAILED로 fail-closed")
    void future_state_timestamp_is_failed() throws Exception {
        write(status(
                DemoState.READY,
                now().plusHours(6),
                false,
                now().plusSeconds(31)
        ));

        assertThat(store().current().state()).isEqualTo(DemoState.FAILED);
    }

    @Test
    @DisplayName("상태 파일과 backend 업로드 정책이 다르면 FAILED")
    void policy_drift_is_failed() throws Exception {
        DemoStatusResponse ready = readyStatus(now().plusHours(6), false);
        write(new DemoStatusResponse(
                ready.enabled(), ready.environmentName(), ready.state(), ready.stateChangedAt(), ready.generation(),
                ready.candidateGeneration(), ready.lastResetAt(), ready.nextResetAt(),
                ready.warningBeforeSeconds(), ready.writeLockBeforeSeconds(), ready.writeLocked(),
                ready.notice(), true, ready.simulatedLocation(), ready.publicAccounts()));

        assertThat(store().current().state()).isEqualTo(DemoState.FAILED);
    }

    @Test
    @DisplayName("READY 상태의 generation·계정·시간 계약이 불완전하면 FAILED")
    void incomplete_ready_contract_is_failed() throws Exception {
        DemoStatusResponse ready = readyStatus(now().plusHours(6), false);
        write(new DemoStatusResponse(
                ready.enabled(), ready.environmentName(), ready.state(), ready.stateChangedAt(), null,
                ready.candidateGeneration(), ready.lastResetAt(), ready.nextResetAt(),
                ready.warningBeforeSeconds(), ready.writeLockBeforeSeconds(), ready.writeLocked(),
                ready.notice(), ready.uploadEnabled(), ready.simulatedLocation(), List.of()));

        assertThat(store().current().state()).isEqualTo(DemoState.FAILED);
        assertThat(store().current().writeLocked()).isTrue();
    }

    @Test
    @DisplayName("FAILED 상태는 초기화 단계에 따라 candidate generation을 생략하거나 보존")
    void failed_state_accepts_optional_candidate_generation() throws Exception {
        DemoStatusResponse failed = status(DemoState.FAILED, null, true);
        for (String candidateGeneration : List.of("", "candidate-generation-2")) {
            write(new DemoStatusResponse(
                    failed.enabled(), failed.environmentName(), failed.state(), failed.stateChangedAt(),
                    failed.generation(), candidateGeneration.isEmpty() ? null : candidateGeneration,
                    failed.lastResetAt(), failed.nextResetAt(), failed.warningBeforeSeconds(),
                    failed.writeLockBeforeSeconds(), failed.writeLocked(), failed.notice(), failed.uploadEnabled(),
                    failed.simulatedLocation(), failed.publicAccounts()));

            DemoStatusResponse status = store().current();
            assertThat(status.state()).isEqualTo(DemoState.FAILED);
            assertThat(status.candidateGeneration()).isEqualTo(
                    candidateGeneration.isEmpty() ? null : candidateGeneration);
        }
    }

    @Test
    @DisplayName("demo off는 파일을 읽지 않고 PRODUCTION 최소 상태를 반환")
    void disabled_status_is_stable() {
        properties.setEnabled(false);

        DemoStatusResponse status = store().current();

        assertThat(status.enabled()).isFalse();
        assertThat(status.environmentName()).isEqualTo("PRODUCTION");
        assertThat(status.generation()).isNull();
        assertThat(status.notice()).isEmpty();
        assertThat(status.writeLocked()).isFalse();
    }

    private DemoStateStore store() {
        return new DemoStateStore(objectMapper, properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(NOW, KST);
    }

    private DemoStatusResponse readyStatus(OffsetDateTime nextResetAt, boolean writeLocked) {
        return status(DemoState.READY, nextResetAt, writeLocked);
    }

    private DemoStatusResponse status(DemoState state, OffsetDateTime nextResetAt, boolean writeLocked) {
        return status(state, nextResetAt, writeLocked, now().minusMinutes(1));
    }

    private DemoStatusResponse status(
            DemoState state,
            OffsetDateTime nextResetAt,
            boolean writeLocked,
            OffsetDateTime stateChangedAt
    ) {
        return new DemoStatusResponse(
                true,
                "DEMO",
                state,
                stateChangedAt,
                "opaque-generation-1",
                state == DemoState.READY ? null : "candidate-generation-2",
                now().minusHours(1),
                nextResetAt,
                300,
                120,
                writeLocked,
                "모든 데이터는 합성 데이터이며 주기적으로 초기화됩니다.",
                false,
                new DemoSimulatedLocation(37.5663, 126.9779),
                List.of(
                        new DemoPublicAccount("관리자", "전체 흐름", "demo.manager", "manager-password", true),
                        new DemoPublicAccount("직원", "개인 흐름", "demo.staff", "staff-password", false)
                )
        );
    }

    private void write(DemoStatusResponse status) throws Exception {
        objectMapper.writeValue(statePath.toFile(), ApiResponse.success(status));
    }
}
