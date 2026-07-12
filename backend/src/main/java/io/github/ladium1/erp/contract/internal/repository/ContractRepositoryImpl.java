package io.github.ladium1.erp.contract.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.entity.Contract;
import io.github.ladium1.erp.contract.internal.entity.QContract;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

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
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), c, c.id.desc()))
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
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(sort, c, c.id.desc()))
                .fetch();
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
}
