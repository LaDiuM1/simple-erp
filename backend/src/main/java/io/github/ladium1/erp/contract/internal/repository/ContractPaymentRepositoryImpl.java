package io.github.ladium1.erp.contract.internal.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.contract.internal.entity.QContractPayment;
import io.github.ladium1.erp.global.validation.MoneyPolicy;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ContractPaymentRepositoryImpl implements ContractPaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> sumPaidAmountByContractIds(Collection<Long> contractIds) {
        if (contractIds == null || contractIds.isEmpty()) {
            return Map.of();
        }
        QContractPayment p = QContractPayment.contractPayment;
        NumberExpression<BigDecimal> paidSum = Expressions.numberTemplate(
                BigDecimal.class, "sum({0})", p.paidAmount);
        List<Tuple> rows = queryFactory
                .select(p.contractId, paidSum)
                .from(p)
                .where(p.contractId.in(contractIds))
                .groupBy(p.contractId)
                .fetch();

        Map<Long, Long> sums = new HashMap<>();
        for (Tuple row : rows) {
            Long contractId = row.get(p.contractId);
            sums.put(contractId, MoneyPolicy.fromAggregate(row.get(paidSum)));
        }
        return sums;
    }
}
