package io.github.ladium1.erp.role.internal.dto;

import lombok.Builder;

@Builder
public record RoleSummaryResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean system
) {
}
