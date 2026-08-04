package io.github.ladium1.erp.contract.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.contract.api.dto.ContractOutstandingSummary;
import io.github.ladium1.erp.contract.api.dto.MonthlyContractStat;
import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.entity.Contract;
import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.QContract;
import io.github.ladium1.erp.contract.internal.entity.QContractPayment;
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
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Contract> search(ContractSearchCondition condition, Pageable pageable) {
        QContract c = QContract.contract;
        BooleanBuilder where = buildPredicate(condition, c);

        List<Contract> content = queryFactory
                .selectFrom(c)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), sortProperties(c), c.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(c.count())
                .from(c)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Contract> searchAll(ContractSearchCondition condition, Sort sort) {
        QContract c = QContract.contract;
        return queryFactory
                .selectFrom(c)
                .where(buildPredicate(condition, c))
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(sort, sortProperties(c), c.id.desc()))
                .fetch();
    }

    @Override
    public List<MonthlyContractStat> monthlyStats(LocalDate fromDate, Set<Long> employeeIdScope) {
        QContract c = QContract.contract;
        // 월 버킷 — JPQL 표준 함수가 아니라 DB (MariaDB) 의 DATE_FORMAT 에 위임.
        StringExpression month = Expressions.stringTemplate("function('date_format', {0}, '%Y-%m')", c.contractDate);
        NumberExpression<BigDecimal> amountSum = Expressions.numberTemplate(
                BigDecimal.class, "sum({0})", c.finalAmount);

        BooleanBuilder where = new BooleanBuilder()
                .and(c.status.ne(ContractStatus.CANCELED))
                .and(c.contractDate.goe(fromDate));
        if (employeeIdScope != null) {
            where.and(c.employeeId.in(employeeIdScope));
        }

        List<Tuple> rows = queryFactory
                .select(month, c.count(), amountSum)
                .from(c)
                .where(where)
                .groupBy(month)
                .orderBy(month.asc())
                .fetch();

        return rows.stream()
                .map(row -> {
                    Long count = row.get(c.count());
                    return MonthlyContractStat.builder()
                            .month(row.get(month))
                            .count(count == null ? 0L : count)
                            .totalAmount(MoneyPolicy.fromAggregate(row.get(amountSum)))
                            .build();
                })
                .toList();
    }

    @Override
    public ContractOutstandingSummary outstandingSummary(Set<Long> employeeIdScope) {
        QContract c = QContract.contract;
        QContractPayment p = QContractPayment.contractPayment;

        BooleanBuilder where = new BooleanBuilder().and(c.status.ne(ContractStatus.CANCELED));
        if (employeeIdScope != null) {
            where.and(c.employeeId.in(employeeIdScope));
        }

        NumberExpression<BigDecimal> finalSum = Expressions.numberTemplate(
                BigDecimal.class, "sum({0})", c.finalAmount);
        BigDecimal totalFinal = queryFactory
                .select(finalSum)
                .from(c)
                .where(where)
                .fetchOne();

        NumberExpression<BigDecimal> paidSum = Expressions.numberTemplate(
                BigDecimal.class, "sum({0})", p.paidAmount);
        BigDecimal totalPaid = queryFactory
                .select(paidSum)
                .from(p)
                .join(c).on(c.id.eq(p.contractId))
                .where(where)
                .fetchOne();

        long finalAmount = MoneyPolicy.fromAggregate(totalFinal);
        long paidAmount = MoneyPolicy.fromAggregate(totalPaid);
        return ContractOutstandingSummary.builder()
                .totalFinalAmount(finalAmount)
                .totalPaidAmount(paidAmount)
                .totalOutstandingAmount(finalAmount - paidAmount)
                .build();
    }

    private BooleanBuilder buildPredicate(ContractSearchCondition condition, QContract c) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.contractNoKeyword())) {
            where.and(c.contractNo.like("%" + condition.contractNoKeyword().trim() + "%"));
        }
        if (condition.customerId() != null) {
            where.and(c.customerId.eq(condition.customerId()));
        }
        if (condition.employeeId() != null) {
            where.and(c.employeeId.eq(condition.employeeId()));
        }
        if (condition.supplierId() != null) {
            where.and(c.supplierId.eq(condition.supplierId()));
        }
        if (condition.status() != null) {
            where.and(c.status.eq(condition.status()));
        }
        if (condition.contractDateFrom() != null) {
            where.and(c.contractDate.goe(condition.contractDateFrom()));
        }
        if (condition.contractDateTo() != null) {
            where.and(c.contractDate.loe(condition.contractDateTo()));
        }
        // 데이터 스코프 — 빈 집합은 service 가 미리 분기하므로 여기는 null (제한 없음) 만 통과.
        if (condition.employeeIdScope() != null) {
            where.and(c.employeeId.in(condition.employeeIdScope()));
        }
        return where;
    }

    private static java.util.Map<String, ? extends com.querydsl.core.types.Expression<? extends Comparable<?>>> sortProperties(QContract c) {
        return java.util.Map.of(
                "id", c.id,
                "contractNo", c.contractNo,
                "contractDate", c.contractDate,
                "finalAmount", c.finalAmount,
                "status", c.status
        );
    }
}
