package io.github.ladium1.erp.position.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PositionUpdateRequest(
        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {
}
