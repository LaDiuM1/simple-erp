package io.github.ladium1.erp.product.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.product.internal.dto.ProductSearchCondition;
import io.github.ladium1.erp.product.internal.entity.Product;
import io.github.ladium1.erp.product.internal.entity.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> search(ProductSearchCondition condition, Pageable pageable) {
        QProduct p = QProduct.product;
        BooleanBuilder where = buildPredicate(condition, p);

        List<Product> content = queryFactory
                .selectFrom(p)
                .leftJoin(p.category).fetchJoin()
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), p, p.modelName.asc()))
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

    @Override
    public Map<Long, Long> countGroupByCategory() {
        QProduct p = QProduct.product;
        List<Tuple> rows = queryFactory
                .select(p.category.id, p.count())
                .from(p)
                .groupBy(p.category.id)
                .fetch();

        Map<Long, Long> counts = new HashMap<>();
        for (Tuple row : rows) {
            Long categoryId = row.get(p.category.id);
            Long count = row.get(p.count());
            counts.put(categoryId, count != null ? count : 0L);
        }
        return counts;
    }

    @Override
    public List<Product> findAllWithCategoryByIds(List<Long> ids) {
        QProduct p = QProduct.product;
        return queryFactory
                .selectFrom(p)
                .leftJoin(p.category).fetchJoin()
                .where(p.id.in(ids))
                .fetch();
    }

    private BooleanBuilder buildPredicate(ProductSearchCondition condition, QProduct p) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.modelNameKeyword())) {
            where.and(p.modelName.like("%" + condition.modelNameKeyword().trim() + "%"));
        }
        if (condition.categoryId() != null) {
            where.and(p.category.id.eq(condition.categoryId()));
        }
        if (condition.supplierId() != null) {
            where.and(p.supplierId.eq(condition.supplierId()));
        }
        if (condition.active() != null) {
            where.and(p.active.eq(condition.active()));
        }
        return where;
    }
}
