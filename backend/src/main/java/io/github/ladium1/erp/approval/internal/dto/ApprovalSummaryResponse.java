package io.github.ladium1.erp.approval.internal.dto;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApprovalSummaryResponse(
        Long id,
        ApprovalDocType docType,
        String title,
        String drafterName,
        ApprovalStatus status,
        LocalDateTime createdAt,
        int currentStepOrder,
        int totalSteps
) {
}
