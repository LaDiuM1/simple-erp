package io.github.ladium1.erp.contract.internal.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContractNoteResponse(
        Long id,
        Long authorEmployeeId,
        String authorName,
        String content,
        LocalDateTime createdAt
) {
}
