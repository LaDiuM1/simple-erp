package io.github.ladium1.erp.salescontact.internal.dto;

import io.github.ladium1.erp.salescontact.internal.entity.AcquisitionSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AcquisitionSourceCreateRequest(
        @NotBlank @Size(max = 100)
        String name,

        @NotNull
        AcquisitionSourceType type,

        @Size(max = 500)
        String description
) {
}
