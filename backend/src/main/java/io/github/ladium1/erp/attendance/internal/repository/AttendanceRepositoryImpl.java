package io.github.ladium1.erp.attendance.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.attendance.internal.dto.AttendanceSearchCondition;
import io.github.ladium1.erp.attendance.internal.entity.Attendance;
import io.github.ladium1.erp.attendance.internal.entity.QAttendance;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class AttendanceRepositoryImpl implements AttendanceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Attendance> search(AttendanceSearchCondition condition, Pageable pageable) {
        QAttendance a = QAttendance.attendance;
        BooleanBuilder where = buildPredicate(condition, a);

        List<Attendance> content = queryFactory
                .selectFrom(a)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), java.util.Map.of(
                        "id", a.id, "workDate", a.workDate), a.workDate.desc()))
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

    private BooleanBuilder buildPredicate(AttendanceSearchCondition condition, QAttendance a) {
        LocalDate monthStart = LocalDate.of(condition.year(), condition.month(), 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        BooleanBuilder where = new BooleanBuilder();
        where.and(a.workDate.between(monthStart, monthEnd));
        if (condition.employeeId() != null) {
            where.and(a.employeeId.eq(condition.employeeId()));
        }
        return where;
    }
}
