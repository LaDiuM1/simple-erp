package io.github.ladium1.erp.department.internal.dto;

import lombok.Builder;

@Builder
public record DepartmentSummaryResponse(
        Long id,
        String code,
        String name,
        String parentName
) {
}
