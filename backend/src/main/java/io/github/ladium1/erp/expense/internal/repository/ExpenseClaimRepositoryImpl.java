package io.github.ladium1.erp.expense.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchCondition;
import io.github.ladium1.erp.expense.internal.entity.ExpenseClaim;
import io.github.ladium1.erp.expense.internal.entity.QExpenseClaim;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class ExpenseClaimRepositoryImpl implements ExpenseClaimRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ExpenseClaim> search(Long claimantId, ExpenseSearchCondition condition, Pageable pageable) {
        QExpenseClaim e = QExpenseClaim.expenseClaim;
        BooleanBuilder where = buildPredicate(claimantId, condition, e);

        List<ExpenseClaim> content = queryFactory
                .selectFrom(e)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), e, e.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(e.count())
                .from(e)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(Long claimantId, ExpenseSearchCondition condition, QExpenseClaim e) {
        BooleanBuilder where = new BooleanBuilder();
        if (claimantId != null) {
            where.and(e.claimantId.eq(claimantId));
        }
        if (condition.status() != null) {
            where.and(e.status.eq(condition.status()));
        }
        if (condition.startDate() != null) {
            where.and(e.createdAt.goe(condition.startDate().atStartOfDay()));
        }
        if (condition.endDate() != null) {
            where.and(e.createdAt.lt(condition.endDate().plusDays(1).atStartOfDay()));
        }
        if (StringUtils.hasText(condition.keyword())) {
            where.and(e.title.like("%" + condition.keyword().trim() + "%"));
        }
        return where;
    }
}
