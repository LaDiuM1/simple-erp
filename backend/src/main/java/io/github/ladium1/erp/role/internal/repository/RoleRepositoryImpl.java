package io.github.ladium1.erp.role.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.role.internal.dto.RoleSearchCondition;
import io.github.ladium1.erp.role.internal.entity.QRole;
import io.github.ladium1.erp.role.internal.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Role> search(RoleSearchCondition condition, Pageable pageable) {
        QRole r = QRole.role;
        BooleanBuilder where = buildPredicate(condition, r);

        List<Role> content = queryFactory
                .selectFrom(r)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), r, r.code.asc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(r.count())
                .from(r)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(RoleSearchCondition condition, QRole r) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.codeKeyword())) {
            where.and(r.code.like("%" + condition.codeKeyword().trim() + "%"));
        }
        if (StringUtils.hasText(condition.nameKeyword())) {
            where.and(r.name.like("%" + condition.nameKeyword().trim() + "%"));
        }
        return where;
    }
}
