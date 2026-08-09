package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

/** 데모 안전 규칙의 단일 정책 경계. 도메인 서비스는 데모 식별자를 직접 하드코딩하지 않는다. */
@Component
@RequiredArgsConstructor
public class DemoProtectionPolicy {

    private final DemoProperties properties;
    private final DemoStateStore stateStore;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public void assertWriteAvailable() {
        if (isEnabled() && stateStore.current().writeLocked()) {
            throw new BusinessException(DemoErrorCode.DEMO_RESET_IN_PROGRESS);
        }
    }

    public boolean isUploadEnabled() {
        return !isEnabled() || properties.getUpload().isEnabled();
    }

    public void assertUploadAllowed() {
        if (!isUploadEnabled()) {
            throw new BusinessException(DemoErrorCode.DEMO_UPLOAD_DISABLED);
        }
    }

    public void assertNoAttachmentIds(Collection<Long> fileIds) {
        if (!isUploadEnabled() && fileIds != null && !fileIds.isEmpty()) {
            throw new BusinessException(DemoErrorCode.DEMO_UPLOAD_DISABLED);
        }
    }

    /** 논리 연결을 지워도 다음 reset 전까지 현재 세대의 seed·방문자 파일을 함께 보존한다. */
    public boolean shouldRetainStoredFiles() {
        return isEnabled();
    }

    public boolean isProtectedEmployee(String loginId) {
        if (!isEnabled() || loginId == null) {
            return false;
        }
        if (properties.getProtection().getProtectedLoginIds().contains(loginId)) {
            return true;
        }
        if (loginId.equals(properties.getProtection().getOperationsAdminLoginId())) {
            return true;
        }
        return stateStore.current().publicAccounts().stream()
                .map(DemoPublicAccount::loginId)
                .filter(Objects::nonNull)
                .anyMatch(loginId::equals);
    }

    /**
     * 데모 계정과 새로 생성된 일반 계정에는 복구 전용 운영자 계정을 노출하지 않는다.
     * 운영자 본인의 직원 관리와 모듈 내부의 과거 기록 이름 조회는 그대로 유지한다.
     */
    public String hiddenOperationsEmployeeLoginId(String viewerLoginId) {
        String operationsLoginId = recoveryOperationsEmployeeLoginId();
        if (operationsLoginId == null || operationsLoginId.equals(viewerLoginId)) {
            return null;
        }
        return operationsLoginId;
    }

    /** 일반 업무 데이터와 신규 참조에서 제외할 복구 전용 운영 계정. */
    public String recoveryOperationsEmployeeLoginId() {
        String operationsLoginId = properties.getProtection().getOperationsAdminLoginId();
        return isEnabled() && StringUtils.hasText(operationsLoginId) ? operationsLoginId : null;
    }

    public boolean isEmployeeHiddenFrom(String viewerLoginId, String employeeLoginId) {
        return Objects.equals(hiddenOperationsEmployeeLoginId(viewerLoginId), employeeLoginId);
    }

    /** 복구 전용 운영자는 일반 업무 데이터의 담당자·결재자 참조로 사용할 수 없다. */
    public boolean isOperationsEmployee(String employeeLoginId) {
        return StringUtils.hasText(employeeLoginId)
                && employeeLoginId.equals(recoveryOperationsEmployeeLoginId());
    }

    public void assertProtectedEmployeeUpdateAllowed(
            String loginId,
            boolean statusChanged,
            boolean roleChanged,
            boolean passwordChanged
    ) {
        if (isProtectedEmployee(loginId) && (statusChanged || roleChanged || passwordChanged)) {
            throw new BusinessException(DemoErrorCode.DEMO_PROTECTED_RESOURCE);
        }
    }

    public void assertEmployeeDeletionAllowed(String loginId) {
        if (isProtectedEmployee(loginId)) {
            throw new BusinessException(DemoErrorCode.DEMO_PROTECTED_RESOURCE);
        }
    }

    public void assertRoleMutationAllowed(String roleCode) {
        if (isEnabled() && properties.getProtection().getProtectedRoleCodes().contains(roleCode)) {
            throw new BusinessException(DemoErrorCode.DEMO_PROTECTED_RESOURCE);
        }
    }

    public String auditIp(String clientIp) {
        if (isEnabled() && !properties.getAudit().isStoreClientIp()) {
            return null;
        }
        return clientIp;
    }

    public DemoSimulatedLocation effectiveLocation(double latitude, double longitude) {
        if (isEnabled() && properties.getGeolocation().isUseSimulatedPosition()) {
            return new DemoSimulatedLocation(
                    properties.getGeolocation().getLatitude(),
                    properties.getGeolocation().getLongitude()
            );
        }
        return new DemoSimulatedLocation(latitude, longitude);
    }
}
