package io.github.ladium1.erp.attendance.internal.dto;

import jakarta.validation.constraints.NotNull;

public record CheckOutRequest(
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
