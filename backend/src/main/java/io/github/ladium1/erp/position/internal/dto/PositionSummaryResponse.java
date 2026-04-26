package io.github.ladium1.erp.position.internal.dto;

import lombok.Builder;

@Builder
public record PositionSummaryResponse(
        Long id,
        String code,
        String name,
        int rankLevel,
        String description
) {
}
