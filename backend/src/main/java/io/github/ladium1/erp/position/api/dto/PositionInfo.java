package io.github.ladium1.erp.position.api.dto;

import lombok.Builder;

@Builder
public record PositionInfo(
        Long id,
        String code,
        String name
) {
}
