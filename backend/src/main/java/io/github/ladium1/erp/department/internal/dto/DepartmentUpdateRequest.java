package io.github.ladium1.erp.department.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentUpdateRequest(
        @NotBlank @Size(max = 100)
        String name,

        Long parentId
) {
}
