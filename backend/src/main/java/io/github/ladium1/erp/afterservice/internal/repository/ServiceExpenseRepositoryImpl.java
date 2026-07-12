package io.github.ladium1.erp.afterservice.internal.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.afterservice.internal.entity.QServiceExpense;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ServiceExpenseRepositoryImpl implements ServiceExpenseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> sumAmountByAfterServiceIds(Collection<Long> afterServiceIds) {
        if (afterServiceIds == null || afterServiceIds.isEmpty()) {
            return Map.of();
        }
        QServiceExpense e = QServiceExpense.serviceExpense;
        NumberExpression<Long> amountSum = e.amount.sumLong();
        List<Tuple> rows = queryFactory
                .select(e.afterServiceId, amountSum)
                .from(e)
                .where(e.afterServiceId.in(afterServiceIds))
                .groupBy(e.afterServiceId)
                .fetch();

        Map<Long, Long> sums = new HashMap<>();
        for (Tuple row : rows) {
            Long afterServiceId = row.get(e.afterServiceId);
            Long sum = row.get(amountSum);
            sums.put(afterServiceId, sum != null ? sum : 0L);
        }
        return sums;
    }
}
