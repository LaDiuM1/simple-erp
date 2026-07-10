package io.github.ladium1.erp.approval.internal.repository;

import io.github.ladium1.erp.approval.internal.dto.ApprovalSearchCondition;
import io.github.ladium1.erp.approval.internal.entity.ApprovalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApprovalDocumentRepositoryCustom {

    /**
     * 결재함 검색 — box 는 현재 사용자 employeeId 기준으로 해석.
     */
    Page<ApprovalDocument> search(Long employeeId, ApprovalSearchCondition condition, Pageable pageable);
}
