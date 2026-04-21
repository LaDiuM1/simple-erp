package io.github.ladium1.erp.role.api.dto;

import lombok.Builder;

@Builder
public record RoleInfo(
        Long id,
        String code,
        String name,
        String description
) {
}
