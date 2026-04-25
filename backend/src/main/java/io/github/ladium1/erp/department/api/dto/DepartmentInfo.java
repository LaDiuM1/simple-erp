package io.github.ladium1.erp.department.api.dto;

import lombok.Builder;

@Builder
public record DepartmentInfo(
        Long id,
        String code,
        String name,
        Long parentId
) {
}
