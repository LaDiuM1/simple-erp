package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.web.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 외부 reset harness 가 원자적으로 교체하는 상태 파일의 read-only adapter.
 * 데모 모드에서 파일이 없거나 계약이 어긋나면 절대 READY 로 추정하지 않는다.
 */
@Slf4j
@Component
public class DemoStateStore {

    private final ObjectMapper objectMapper;
    private final DemoProperties properties;
    private final Clock clock;
    private final JavaType envelopeType;

    @Autowired
    public DemoStateStore(ObjectMapper objectMapper, DemoProperties properties) {
        this(objectMapper, properties, Clock.systemUTC());
    }

    DemoStateStore(ObjectMapper objectMapper, DemoProperties properties, Clock clock) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.clock = clock;
        this.envelopeType = objectMapper.getTypeFactory()
                .constructParametricType(ApiResponse.class, DemoStatusResponse.class);
    }

    public DemoStatusResponse current() {
        if (!properties.isEnabled()) {
            return disabledStatus();
        }

        Path statePath = Path.of(properties.getStatePath()).toAbsolutePath().normalize();
        try {
            if (!Files.isRegularFile(statePath)) {
                return failedStatus();
            }
            ApiResponse<DemoStatusResponse> envelope = objectMapper.readValue(statePath.toFile(), envelopeType);
            DemoStatusResponse status = envelope.data();
            validateEnvelope(envelope, status);
            return applyRuntimeLock(status);
        } catch (RuntimeException invalidState) {
            log.warn("데모 상태 파일을 사용할 수 없어 fail-closed 처리: path={}, reason={}",
                    statePath, invalidState.getMessage());
            return failedStatus();
        }
    }

    private void validateEnvelope(ApiResponse<DemoStatusResponse> envelope, DemoStatusResponse status) {
        if (envelope == null || envelope.status() != 200 || status == null) {
            throw new IllegalStateException("상태 envelope가 유효하지 않음");
        }
        if (!status.enabled() || status.state() == null || status.stateChangedAt() == null) {
            throw new IllegalStateException("데모 enabled/state/stateChangedAt 누락");
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (status.stateChangedAt().toInstant().isAfter(now.toInstant().plusSeconds(30))) {
            throw new IllegalStateException("stateChangedAt이 현재보다 미래임");
        }
        if (isBlank(status.notice())) {
            throw new IllegalStateException("데모 notice 누락");
        }
        if (!properties.getEnvironmentName().equals(status.environmentName())) {
            throw new IllegalStateException("데모 환경 이름 불일치");
        }
        long warningSeconds = properties.getReset().getWarningBefore().toSeconds();
        long writeLockSeconds = properties.getReset().getWriteLockBefore().toSeconds();
        if (status.warningBeforeSeconds() != warningSeconds
                || status.writeLockBeforeSeconds() != writeLockSeconds
                || warningSeconds < writeLockSeconds
                || writeLockSeconds <= 0) {
            throw new IllegalStateException("reset 경계 설정 불일치");
        }
        if (status.uploadEnabled() != properties.getUpload().isEnabled()) {
            throw new IllegalStateException("업로드 정책 불일치");
        }
        if (properties.getGeolocation().isUseSimulatedPosition()) {
            DemoSimulatedLocation location = status.simulatedLocation();
            if (location == null
                    || Double.compare(location.latitude(), properties.getGeolocation().getLatitude()) != 0
                    || Double.compare(location.longitude(), properties.getGeolocation().getLongitude()) != 0) {
                throw new IllegalStateException("모의 위치 설정 불일치");
            }
        } else if (status.simulatedLocation() != null) {
            throw new IllegalStateException("모의 위치 설정 불일치");
        }
        if (status.state() == DemoState.READY
                && (isBlank(status.generation())
                || status.candidateGeneration() != null
                || status.lastResetAt() == null
                || status.nextResetAt() == null
                || status.publicAccounts().isEmpty())) {
            throw new IllegalStateException("READY 상태 계약 누락");
        }
        if ((status.state() == DemoState.RESETTING || status.state() == DemoState.VERIFYING)
                && (isBlank(status.candidateGeneration()) || !status.writeLocked())) {
            throw new IllegalStateException("maintenance 상태 계약 누락");
        }
        if (status.state() == DemoState.FAILED && !status.writeLocked()) {
            throw new IllegalStateException("FAILED 상태 계약 누락");
        }
        if (status.lastResetAt() != null
                && status.lastResetAt().toInstant().isAfter(status.stateChangedAt().toInstant())) {
            throw new IllegalStateException("stateChangedAt보다 미래인 lastResetAt");
        }
        if (status.lastResetAt() != null && status.nextResetAt() != null
                && !status.lastResetAt().isBefore(status.nextResetAt())) {
            throw new IllegalStateException("reset 시간 순서 불일치");
        }
        Set<String> loginIds = new HashSet<>();
        for (DemoPublicAccount account : status.publicAccounts()) {
            if (account == null
                    || isBlank(account.label())
                    || account.description() == null
                    || isBlank(account.loginId())
                    || isBlank(account.password())
                    || !loginIds.add(account.loginId())) {
                throw new IllegalStateException("데모 계정 계약 불일치");
            }
        }
    }

    private DemoStatusResponse applyRuntimeLock(DemoStatusResponse status) {
        if ((status.state() == DemoState.RESETTING || status.state() == DemoState.VERIFYING)
                && !clock.instant().isBefore(
                        status.stateChangedAt().toInstant()
                                .plus(properties.getReset().getOperationTimeout()))) {
            return failedStatus();
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        if (status.state() == DemoState.READY && !now.isBefore(status.nextResetAt())) {
            return failedStatus();
        }

        boolean scheduledLock = status.nextResetAt() != null
                && !now.isBefore(status.nextResetAt().minusSeconds(status.writeLockBeforeSeconds()));
        boolean writeLocked = status.writeLocked()
                || status.state() != DemoState.READY
                || scheduledLock;

        return new DemoStatusResponse(
                true,
                status.environmentName(),
                status.state(),
                status.stateChangedAt(),
                status.generation(),
                status.candidateGeneration(),
                status.lastResetAt(),
                status.nextResetAt(),
                status.warningBeforeSeconds(),
                status.writeLockBeforeSeconds(),
                writeLocked,
                status.notice(),
                status.uploadEnabled(),
                status.simulatedLocation(),
                status.publicAccounts()
        );
    }

    private DemoStatusResponse disabledStatus() {
        return new DemoStatusResponse(
                false,
                "PRODUCTION",
                DemoState.READY,
                null,
                null,
                null,
                null,
                null,
                properties.getReset().getWarningBefore().toSeconds(),
                properties.getReset().getWriteLockBefore().toSeconds(),
                false,
                "",
                true,
                null,
                List.of()
        );
    }

    private DemoStatusResponse failedStatus() {
        DemoSimulatedLocation simulatedLocation = properties.getGeolocation().isUseSimulatedPosition()
                ? new DemoSimulatedLocation(
                        properties.getGeolocation().getLatitude(),
                        properties.getGeolocation().getLongitude())
                : null;
        return new DemoStatusResponse(
                true,
                properties.getEnvironmentName(),
                DemoState.FAILED,
                OffsetDateTime.now(clock),
                null,
                null,
                null,
                null,
                properties.getReset().getWarningBefore().toSeconds(),
                properties.getReset().getWriteLockBefore().toSeconds(),
                true,
                properties.getNotice(),
                properties.getUpload().isEnabled(),
                simulatedLocation,
                List.of()
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
