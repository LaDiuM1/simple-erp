package io.github.ladium1.erp.customer.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.customer.internal.dto.CustomerSearchCondition;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import io.github.ladium1.erp.customer.internal.entity.QCustomer;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * FE 응답 DTO 가 평탄화 (예: roadAddress) 한 필드를 엔티티 임베디드 경로 (address.roadAddress) 로 매핑.
     * 향후 다른 임베디드 컬럼이 정렬 대상이 되면 여기에 항목 추가.
     */
    private static final java.util.Map<String, String> SORT_PROPERTY_ALIAS = java.util.Map.of(
            "roadAddress", "address.roadAddress"
    );

    @Override
    public Page<Customer> search(CustomerSearchCondition condition, Pageable pageable) {
        QCustomer c = QCustomer.customer;
        BooleanBuilder where = buildPredicate(condition, c);

        List<Customer> content = queryFactory
                .selectFrom(c)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(remapSort(pageable.getSort()), c, c.id.desc()))
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
    public List<Customer> searchAll(CustomerSearchCondition condition, Sort sort) {
        QCustomer c = QCustomer.customer;
        return queryFactory
                .selectFrom(c)
                .where(buildPredicate(condition, c))
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(remapSort(sort), c, c.id.desc()))
                .fetch();
    }

    private static Sort remapSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return sort;
        }
        List<Sort.Order> remapped = sort.stream()
                .map(o -> {
                    String mapped = SORT_PROPERTY_ALIAS.get(o.getProperty());
                    return mapped == null ? o : new Sort.Order(o.getDirection(), mapped);
                })
                .toList();
        return Sort.by(remapped);
    }

    private BooleanBuilder buildPredicate(CustomerSearchCondition condition, QCustomer c) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.codeKeyword())) {
            where.and(c.code.like("%" + condition.codeKeyword().trim() + "%"));
        }
        if (StringUtils.hasText(condition.nameKeyword())) {
            where.and(c.name.like("%" + condition.nameKeyword().trim() + "%"));
        }
        if (StringUtils.hasText(condition.addressKeyword())) {
            String like = "%" + condition.addressKeyword().trim() + "%";
            where.and(c.address.roadAddress.like(like).or(c.address.detailAddress.like(like)));
        }
        if (StringUtils.hasText(condition.phoneKeyword())) {
            String like = "%" + condition.phoneKeyword().trim() + "%";
            where.and(c.phone.like(like).or(c.fax.like(like)));
        }
        if (condition.type() != null) {
            where.and(c.type.eq(condition.type()));
        }
        if (condition.status() != null) {
            where.and(c.status.eq(condition.status()));
        }
        return where;
    }
}
