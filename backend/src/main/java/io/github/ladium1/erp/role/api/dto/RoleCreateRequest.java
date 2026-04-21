package io.github.ladium1.erp.role.api.dto;

import lombok.Builder;

@Builder
public record RoleCreateRequest(
        String code,
        String name,
        String description
) {
}
