package io.github.ladium1.erp.approval.api.dto;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import lombok.Builder;

import java.util.List;

/**
 * 결재 상신 명령 — 타 모듈이 자기 레코드를 결재에 태울 때 전달.
 *
 * @param refId             연동 도메인 레코드 ID (GENERAL 기안은 null)
 * @param approverIds       결재선 — 순서 그대로 1단계부터 결재
 * @param attachmentFileIds 첨부 파일 ID 목록 (없으면 null 또는 빈 리스트)
 */
@Builder
public record ApprovalSubmitCommand(
        ApprovalDocType docType,
        String title,
        String content,
        Long drafterId,
        Long refId,
        List<Long> approverIds,
        List<Long> attachmentFileIds
) {
}
