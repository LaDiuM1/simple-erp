package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.EngineerType;
import lombok.Builder;

@Builder
public record EngineerResponse(
        Long id,
        String name,
        EngineerType type,
        String affiliation,
        String phone,
        Long employeeId,
        String employeeName,
        boolean active
) {
}
