package io.github.ladium1.erp.department.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.department.internal.dto.DepartmentSearchCondition;
import io.github.ladium1.erp.department.internal.entity.Department;
import io.github.ladium1.erp.department.internal.entity.QDepartment;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Department> search(DepartmentSearchCondition condition, Pageable pageable) {
        QDepartment d = QDepartment.department;
        BooleanBuilder where = buildPredicate(condition, d);

        List<Department> content = queryFactory
                .selectFrom(d)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), d, d.code.asc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(d.count())
                .from(d)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(DepartmentSearchCondition condition, QDepartment d) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.keyword())) {
            String like = "%" + condition.keyword().trim() + "%";
            where.and(d.code.like(like).or(d.name.like(like)));
        }
        return where;
    }
}
