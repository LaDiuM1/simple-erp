package io.github.ladium1.erp.approval.internal.dto;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStatus;

/**
 * 결재 문서 목록 검색 조건 — box 만 필수.
 */
public record ApprovalSearchCondition(
        ApprovalBox box,
        ApprovalStatus status,
        ApprovalDocType docType,
        String keyword
) {
}
