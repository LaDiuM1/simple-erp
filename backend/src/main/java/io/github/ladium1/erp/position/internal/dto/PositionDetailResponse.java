package io.github.ladium1.erp.position.internal.dto;

import lombok.Builder;

@Builder
public record PositionDetailResponse(
        Long id,
        String code,
        String name,
        int rankLevel,
        String description
) {
}
