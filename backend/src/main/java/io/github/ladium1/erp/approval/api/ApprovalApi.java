package io.github.ladium1.erp.approval.api;

import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;

/**
 * 전자결재 공개 API — 타 모듈이 자기 문서 (경비 / 휴가 등) 를 결재에 태울 때 사용.
 */
public interface ApprovalApi {

    /**
     * 결재 문서 생성 + 결재선 구성 + 상신 (IN_PROGRESS).
     *
     * @return 생성된 approvalDocumentId
     */
    Long submit(ApprovalSubmitCommand command);

    /**
     * 해당 문서의 관련자 (기안자 또는 결재선 포함) 여부 — 소비 도메인이 원본 / 첨부 접근 통제에 사용.
     * 문서가 없으면 false.
     */
    boolean involves(Long documentId, Long employeeId);
}
