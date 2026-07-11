package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.LeaveStatus;

import java.time.LocalDate;

/**
 * 관리자 휴가 신청 검색 조건 — 전부 선택 필터, startDate / endDate 는 신청 기간과의 겹침 (overlap) 판정.
 */
public record LeaveSearchCondition(
        LeaveStatus status,
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate
) {
}
