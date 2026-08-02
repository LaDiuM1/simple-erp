package io.github.ladium1.erp.employee.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSearchCondition;
import io.github.ladium1.erp.employee.internal.entity.Employee;
import io.github.ladium1.erp.employee.internal.entity.QEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Employee> search(EmployeeSearchCondition condition, Pageable pageable) {
        return searchVisible(condition, null, pageable);
    }

    @Override
    public Page<Employee> searchVisible(
            EmployeeSearchCondition condition,
            Set<Long> visibleEmployeeIds,
            Pageable pageable
    ) {
        QEmployee m = QEmployee.employee;
        BooleanBuilder where = buildPredicate(condition, m);
        if (visibleEmployeeIds != null) {
            where.and(m.id.in(visibleEmployeeIds));
        }

        List<Employee> content = queryFactory
                .selectFrom(m)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), sortProperties(m), m.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(m.count())
                .from(m)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Employee> searchAll(EmployeeSearchCondition condition, Sort sort) {
        QEmployee m = QEmployee.employee;
        return queryFactory
                .selectFrom(m)
                .where(buildPredicate(condition, m))
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(sort, sortProperties(m), m.id.desc()))
                .fetch();
    }

    private BooleanBuilder buildPredicate(EmployeeSearchCondition condition, QEmployee m) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.loginIdKeyword())) {
            where.and(m.loginId.like("%" + condition.loginIdKeyword().trim() + "%"));
        }
        if (StringUtils.hasText(condition.nameKeyword())) {
            where.and(m.name.like("%" + condition.nameKeyword().trim() + "%"));
        }
        if (condition.departmentId() != null) {
            where.and(m.departmentId.eq(condition.departmentId()));
        }
        if (condition.positionId() != null) {
            where.and(m.positionId.eq(condition.positionId()));
        }
        if (condition.roleId() != null) {
            where.and(m.roleId.eq(condition.roleId()));
        }
        if (condition.status() != null) {
            where.and(m.status.eq(condition.status()));
        }
        return where;
    }

    private static java.util.Map<String, ? extends com.querydsl.core.types.Expression<? extends Comparable<?>>> sortProperties(QEmployee m) {
        return java.util.Map.of(
                "id", m.id,
                "name", m.name,
                "loginId", m.loginId,
                "joinDate", m.joinDate,
                "status", m.status
        );
    }
}
