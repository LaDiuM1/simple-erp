package io.github.ladium1.erp.attendance.internal.dto;

import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

/**
 * 근태 수동 정정 요청 — 두 시각 중 최소 하나는 필수, null 인 값은 기존 기록 유지.
 */
public record AttendanceCorrectRequest(
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt
) {

    @AssertTrue(message = "정정할 시각을 최소 하나 입력해야 합니다.")
    public boolean isAnyFieldPresent() {
        return checkInAt != null || checkOutAt != null;
    }
}
