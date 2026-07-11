package io.github.ladium1.erp.afterservice.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.entity.AfterService;
import io.github.ladium1.erp.afterservice.internal.entity.QAfterService;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

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
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), a, a.id.desc()))
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
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(sort, a, a.id.desc()))
                .fetch();
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
}
