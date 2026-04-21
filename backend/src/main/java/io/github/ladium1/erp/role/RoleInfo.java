package io.github.ladium1.erp.role;

import lombok.Builder;

@Builder
public record RoleInfo(
        Long id,
        String code,
        String name,
        String description
) {
}
