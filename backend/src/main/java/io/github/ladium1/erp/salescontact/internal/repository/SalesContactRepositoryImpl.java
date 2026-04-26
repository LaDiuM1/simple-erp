package io.github.ladium1.erp.salescontact.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSearchCondition;
import io.github.ladium1.erp.salescontact.internal.entity.QSalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class SalesContactRepositoryImpl implements SalesContactRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SalesContact> search(SalesContactSearchCondition condition, Pageable pageable) {
        QSalesContact c = QSalesContact.salesContact;
        BooleanBuilder where = buildPredicate(condition, c);

        List<SalesContact> content = queryFactory
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

    private BooleanBuilder buildPredicate(SalesContactSearchCondition condition, QSalesContact c) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.nameKeyword())) {
            where.and(c.name.like("%" + condition.nameKeyword().trim() + "%"));
        }
        if (StringUtils.hasText(condition.emailKeyword())) {
            String like = "%" + condition.emailKeyword().trim() + "%";
            where.and(c.email.like(like).or(c.personalEmail.like(like)));
        }
        if (StringUtils.hasText(condition.phoneKeyword())) {
            String like = "%" + condition.phoneKeyword().trim() + "%";
            where.and(c.mobilePhone.like(like).or(c.officePhone.like(like)));
        }
        return where;
    }
}
