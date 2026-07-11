package io.github.ladium1.erp.approval.internal.dto;

import io.github.ladium1.erp.approval.internal.entity.StepStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApprovalStepResponse(
        int stepOrder,
        Long approverId,
        String approverName,
        StepStatus status,
        String comment,
        LocalDateTime decidedAt
) {
}
