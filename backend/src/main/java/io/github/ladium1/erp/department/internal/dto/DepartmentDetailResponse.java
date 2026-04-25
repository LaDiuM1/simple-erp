package io.github.ladium1.erp.department.internal.dto;

import lombok.Builder;

@Builder
public record DepartmentDetailResponse(
        Long id,
        String code,
        String name,
        Long parentId,
        String parentName
) {
}
