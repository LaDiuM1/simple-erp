package io.github.ladium1.erp.approval.internal.dto;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결재 문서 상세 — myTurn / cancelable 은 현재 사용자 관점 플래그.
 */
@Builder
public record ApprovalDetailResponse(
        Long id,
        ApprovalDocType docType,
        String title,
        String content,
        Long drafterId,
        String drafterName,
        Long refId,
        ApprovalStatus status,
        int currentStepOrder,
        LocalDateTime createdAt,
        List<ApprovalStepResponse> steps,
        List<ApprovalAttachmentResponse> attachments,
        boolean myTurn,
        boolean cancelable
) {
}
