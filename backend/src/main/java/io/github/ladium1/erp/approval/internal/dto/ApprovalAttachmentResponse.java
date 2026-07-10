package io.github.ladium1.erp.approval.internal.dto;

import lombok.Builder;

@Builder
public record ApprovalAttachmentResponse(
        Long fileId,
        String name,
        long size
) {
}
