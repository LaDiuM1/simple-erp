package io.github.ladium1.erp.afterservice.internal.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ServiceVisitResponse(
        Long id,
        LocalDate visitDate,
        Long engineerId,
        String engineerName,
        String problem,
        String resolution
) {
}
