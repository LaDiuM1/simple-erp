package io.github.ladium1.erp.position.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.position.internal.dto.PositionSearchCondition;
import io.github.ladium1.erp.position.internal.entity.Position;
import io.github.ladium1.erp.position.internal.entity.QPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PositionRepositoryImpl implements PositionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Position> search(PositionSearchCondition condition, Pageable pageable) {
        QPosition p = QPosition.position;
        BooleanBuilder where = buildPredicate(condition, p);

        List<Position> content = queryFactory
                .selectFrom(p)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), p, p.rankLevel.asc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(p.count())
                .from(p)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(PositionSearchCondition condition, QPosition p) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.keyword())) {
            String like = "%" + condition.keyword().trim() + "%";
            where.and(p.code.like(like).or(p.name.like(like)));
        }
        return where;
    }
}
