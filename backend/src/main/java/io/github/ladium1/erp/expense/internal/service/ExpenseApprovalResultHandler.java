package io.github.ladium1.erp.expense.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.ApprovalResultHandler;
import io.github.ladium1.erp.expense.internal.entity.ExpenseClaim;
import io.github.ladium1.erp.expense.internal.exception.ExpenseErrorCode;
import io.github.ladium1.erp.expense.internal.repository.ExpenseClaimRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전자결재 결과 콜백 — EXPENSE 문서의 최종 승인 / 반려를 경비 청구 상태로 반영.
 */
@Component
@RequiredArgsConstructor
public class ExpenseApprovalResultHandler implements ApprovalResultHandler {

    private final ExpenseClaimRepository expenseClaimRepository;

    @Override
    public ApprovalDocType docType() {
        return ApprovalDocType.EXPENSE;
    }

    @Override
    @Transactional
    public void onApproved(Long refId) {
        getClaim(refId).approve();
    }

    @Override
    @Transactional
    public void onRejected(Long refId) {
        getClaim(refId).reject();
    }

    private ExpenseClaim getClaim(Long refId) {
        return expenseClaimRepository.findById(refId)
                .orElseThrow(() -> new BusinessException(ExpenseErrorCode.CLAIM_NOT_FOUND));
    }
}
