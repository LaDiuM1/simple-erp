package io.github.ladium1.erp.role;

import lombok.Builder;

@Builder
public record RoleCreateRequest(
        String code,
        String name,
        String description
) {
}
