package io.github.ladium1.erp.afterservice.api.dto;

import lombok.Builder;

/**
 * 엔지니어별 AS 원가 — 대시보드 위젯용. 엔지니어 미지정 경비 (부품비 등) 는 제외.
 */
@Builder
public record EngineerExpenseStat(
        Long engineerId,
        String engineerName,
        /** Σ경비 (원) */
        long expenseTotal
) {
}
