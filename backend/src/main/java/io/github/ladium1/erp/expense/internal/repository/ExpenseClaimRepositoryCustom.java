package io.github.ladium1.erp.expense.internal.repository;

import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchCondition;
import io.github.ladium1.erp.expense.internal.entity.ExpenseClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseClaimRepositoryCustom {

    /**
     * 경비 청구 검색 — claimantId 가 있으면 본인 청구만, null 이면 전체 (정산 관리자 전용 조회).
     */
    Page<ExpenseClaim> search(Long claimantId, ExpenseSearchCondition condition, Pageable pageable);
}
