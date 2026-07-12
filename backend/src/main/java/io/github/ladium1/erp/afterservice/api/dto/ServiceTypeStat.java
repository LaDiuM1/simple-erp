package io.github.ladium1.erp.afterservice.api.dto;

import lombok.Builder;

/**
 * AS 유형별 건수 / 원가 — 대시보드 위젯용. 모듈 내부 enum 을 노출하지 않기 위해 name / 라벨 문자열 전달.
 */
@Builder
public record ServiceTypeStat(
        String type,
        String typeLabel,
        long count,
        /** Σ경비 (원) */
        long expenseTotal
) {
}
