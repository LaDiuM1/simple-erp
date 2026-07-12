package io.github.ladium1.erp.contract.internal.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.contract.internal.entity.QContractPayment;
import lombok.RequiredArgsConstructor;

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
        NumberExpression<Long> paidSum = p.paidAmount.sumLong();
        List<Tuple> rows = queryFactory
                .select(p.contractId, paidSum)
                .from(p)
                .where(p.contractId.in(contractIds))
                .groupBy(p.contractId)
                .fetch();

        Map<Long, Long> sums = new HashMap<>();
        for (Tuple row : rows) {
            Long contractId = row.get(p.contractId);
            Long sum = row.get(paidSum);
            sums.put(contractId, sum != null ? sum : 0L);
        }
        return sums;
    }
}
