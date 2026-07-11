package io.github.ladium1.erp.supplier.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.supplier.internal.dto.SupplierSearchCondition;
import io.github.ladium1.erp.supplier.internal.entity.QSupplier;
import io.github.ladium1.erp.supplier.internal.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class SupplierRepositoryImpl implements SupplierRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Supplier> search(SupplierSearchCondition condition, Pageable pageable) {
        QSupplier s = QSupplier.supplier;
        BooleanBuilder where = buildPredicate(condition, s);

        List<Supplier> content = queryFactory
                .selectFrom(s)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), s, s.name.asc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(s.count())
                .from(s)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(SupplierSearchCondition condition, QSupplier s) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.keyword())) {
            String pattern = "%" + condition.keyword().trim() + "%";
            // 사용자는 영문 / 한글 표기 구분 없이 하나의 검색어로 찾으므로 두 컬럼 통합 검색.
            where.and(s.name.like(pattern).or(s.nameKo.like(pattern)));
        }
        if (condition.active() != null) {
            where.and(s.active.eq(condition.active()));
        }
        return where;
    }
}
