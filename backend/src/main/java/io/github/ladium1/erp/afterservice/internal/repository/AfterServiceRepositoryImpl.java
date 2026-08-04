package io.github.ladium1.erp.afterservice.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.entity.AfterService;
import io.github.ladium1.erp.afterservice.internal.entity.QAfterService;
import io.github.ladium1.erp.afterservice.internal.entity.QServiceExpense;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.global.validation.MoneyPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AfterServiceRepositoryImpl implements AfterServiceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AfterService> search(AfterServiceSearchCondition condition, Pageable pageable) {
        QAfterService a = QAfterService.afterService;
        BooleanBuilder where = buildPredicate(condition, a);

        List<AfterService> content = queryFactory
                .selectFrom(a)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), sortProperties(a), a.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(a.count())
                .from(a)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<AfterService> searchAll(AfterServiceSearchCondition condition, Sort sort) {
        QAfterService a = QAfterService.afterService;
        return queryFactory
                .selectFrom(a)
                .where(buildPredicate(condition, a))
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(sort, sortProperties(a), a.id.desc()))
                .fetch();
    }

    @Override
    public Map<ServiceType, Long> countByTypeSince(LocalDate fromDate) {
        QAfterService a = QAfterService.afterService;
        List<Tuple> rows = queryFactory
                .select(a.type, a.count())
                .from(a)
                .where(a.receivedDate.goe(fromDate))
                .groupBy(a.type)
                .fetch();

        Map<ServiceType, Long> counts = new HashMap<>();
        for (Tuple row : rows) {
            Long count = row.get(a.count());
            counts.put(row.get(a.type), count != null ? count : 0L);
        }
        return counts;
    }

    @Override
    public Map<ServiceType, Long> expenseSumByTypeSince(LocalDate fromDate) {
        QAfterService a = QAfterService.afterService;
        QServiceExpense e = QServiceExpense.serviceExpense;
        NumberExpression<BigDecimal> amountSum = Expressions.numberTemplate(
                BigDecimal.class, "sum({0})", e.amount);
        List<Tuple> rows = queryFactory
                .select(a.type, amountSum)
                .from(e)
                .join(a).on(a.id.eq(e.afterServiceId))
                .where(a.receivedDate.goe(fromDate))
                .groupBy(a.type)
                .fetch();

        Map<ServiceType, Long> sums = new HashMap<>();
        for (Tuple row : rows) {
            sums.put(row.get(a.type), MoneyPolicy.fromAggregate(row.get(amountSum)));
        }
        return sums;
    }

    @Override
    public Map<Long, Long> expenseSumByEngineerSince(LocalDate fromDate) {
        QAfterService a = QAfterService.afterService;
        QServiceExpense e = QServiceExpense.serviceExpense;
        NumberExpression<BigDecimal> amountSum = Expressions.numberTemplate(
                BigDecimal.class, "sum({0})", e.amount);
        List<Tuple> rows = queryFactory
                .select(e.engineerId, amountSum)
                .from(e)
                .join(a).on(a.id.eq(e.afterServiceId))
                .where(a.receivedDate.goe(fromDate).and(e.engineerId.isNotNull()))
                .groupBy(e.engineerId)
                .fetch();

        Map<Long, Long> sums = new HashMap<>();
        for (Tuple row : rows) {
            sums.put(row.get(e.engineerId), MoneyPolicy.fromAggregate(row.get(amountSum)));
        }
        return sums;
    }

    private BooleanBuilder buildPredicate(AfterServiceSearchCondition condition, QAfterService a) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.receiptNoKeyword())) {
            where.and(a.receiptNo.like("%" + condition.receiptNoKeyword().trim() + "%"));
        }
        if (condition.customerId() != null) {
            where.and(a.customerId.eq(condition.customerId()));
        }
        if (condition.type() != null) {
            where.and(a.type.eq(condition.type()));
        }
        if (condition.status() != null) {
            where.and(a.status.eq(condition.status()));
        }
        if (condition.warrantyDecision() != null) {
            where.and(a.warrantyDecision.eq(condition.warrantyDecision()));
        }
        if (condition.engineerId() != null) {
            where.and(a.assignedEngineerId.eq(condition.engineerId()));
        }
        if (condition.receivedDateFrom() != null) {
            where.and(a.receivedDate.goe(condition.receivedDateFrom()));
        }
        if (condition.receivedDateTo() != null) {
            where.and(a.receivedDate.loe(condition.receivedDateTo()));
        }
        return where;
    }

    private static java.util.Map<String, ? extends com.querydsl.core.types.Expression<? extends Comparable<?>>> sortProperties(QAfterService a) {
        return java.util.Map.of(
                "id", a.id,
                "receiptNo", a.receiptNo,
                "receivedDate", a.receivedDate,
                "status", a.status
        );
    }
}
