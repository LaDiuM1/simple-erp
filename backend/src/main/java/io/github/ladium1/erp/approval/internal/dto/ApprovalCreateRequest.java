package io.github.ladium1.erp.approval.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * GENERAL 기안 작성 요청 — 기안자는 현재 사용자.
 */
public record ApprovalCreateRequest(
        @NotBlank String title,
        String content,
        @NotEmpty List<Long> approverIds,
        List<Long> attachmentFileIds
) {
}
