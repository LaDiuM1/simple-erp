package io.github.ladium1.erp.afterservice.internal.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 방문 일지 등록 / 수정 공용 요청 — 두 케이스의 입력 항목이 동일.
 */
public record ServiceVisitRequest(
        @NotNull
        LocalDate visitDate,

        @NotNull
        Long engineerId,

        String problem,

        String resolution
) {
}
