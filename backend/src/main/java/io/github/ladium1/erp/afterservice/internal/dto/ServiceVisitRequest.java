package io.github.ladium1.erp.afterservice.internal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 방문 일지 등록 / 수정 공용 요청 — 두 케이스의 입력 항목이 동일.
 */
public record ServiceVisitRequest(
        @NotNull
        LocalDate visitDate,

        @NotNull
        Long engineerId,

        @Size(max = 4000)
        String problem,

        @Size(max = 4000)
        String resolution
) {
}
