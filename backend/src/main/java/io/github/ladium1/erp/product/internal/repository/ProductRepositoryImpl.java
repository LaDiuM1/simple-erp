package io.github.ladium1.erp.product.internal.repository;

import com.querydsl.core.BooleanBuilder;
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

import java.util.List;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> search(ProductSearchCondition condition, Pageable pageable) {
        QProduct p = QProduct.product;
        BooleanBuilder where = buildPredicate(condition, p);

        List<Product> content = queryFactory
                .selectFrom(p)
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

    private BooleanBuilder buildPredicate(ProductSearchCondition condition, QProduct p) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.modelNameKeyword())) {
            where.and(p.modelName.like("%" + condition.modelNameKeyword().trim() + "%"));
        }
        if (condition.category() != null) {
            where.and(p.category.eq(condition.category()));
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
