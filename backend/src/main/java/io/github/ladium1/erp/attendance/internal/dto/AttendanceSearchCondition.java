package io.github.ladium1.erp.attendance.internal.dto;

/**
 * 전 직원 근태 현황 검색 조건 — 기간 (year / month) 필수, employeeId 는 선택 필터.
 */
public record AttendanceSearchCondition(
        int year,
        int month,
        Long employeeId
) {
}
