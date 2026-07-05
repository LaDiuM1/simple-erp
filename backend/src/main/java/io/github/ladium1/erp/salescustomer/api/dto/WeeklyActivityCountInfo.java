package io.github.ladium1.erp.salescustomer.api.dto;

import lombok.Builder;

import java.time.LocalDate;

/**
 * 주 단위 영업 활동 집계 — 대시보드 활동 추이 차트용. weekStart 는 해당 주의 월요일.
 */
@Builder
public record WeeklyActivityCountInfo(
        LocalDate weekStart,
        long count
) {
}
