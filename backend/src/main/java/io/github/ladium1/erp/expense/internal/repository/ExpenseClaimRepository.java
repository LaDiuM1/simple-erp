package io.github.ladium1.erp.expense.internal.repository;

import io.github.ladium1.erp.expense.internal.entity.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long>, ExpenseClaimRepositoryCustom {
}
